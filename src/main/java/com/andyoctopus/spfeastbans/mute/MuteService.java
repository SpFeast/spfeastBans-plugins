package com.andyoctopus.spfeastbans.mute;

import com.andyoctopus.spfeastbans.SpfeastBansPlugin;
import com.andyoctopus.spfeastbans.util.DurationParser;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public final class MuteService {
    private static final char[] MUTE_ID_CHARSET = "ABCDEF0123456789".toCharArray();

    private final SpfeastBansPlugin plugin;
    private final MuteStorage storage;
    private final MuteHistoryStorage historyStorage;
    private final DateTimeFormatter dateFormatter;
    private final Random random = new SecureRandom();

    public MuteService(SpfeastBansPlugin plugin, MuteStorage storage, MuteHistoryStorage historyStorage) {
        this.plugin = plugin;
        this.storage = storage;
        this.historyStorage = historyStorage;
        this.storage.setLogger(plugin.getLogger());
        this.historyStorage.setLogger(plugin.getLogger());
        this.dateFormatter = DateTimeFormatter.ofPattern(plugin.getConfig().getString("date-format", "yyyy-MM-dd HH:mm:ss"))
                .withZone(ZoneId.systemDefault());
        cleanupExpiredMutes();
    }

    public MuteEntry mute(UUID uniqueId, String playerName, MuteReason reason, String actor, long expiresAtMillis) {
        MuteEntry entry = new MuteEntry(
                uniqueId,
                playerName,
                reason.getKey(),
                actor,
                generateMuteId(),
                System.currentTimeMillis(),
                expiresAtMillis
        );
        storage.put(entry);
        storage.save();
        historyStorage.append(entry);
        historyStorage.save();
        Player player = plugin.getServer().getPlayer(uniqueId);
        if (player != null && player.isOnline()) {
            sendMuteNotificationIfPending(player, entry);
        }
        return entry;
    }

    public boolean unmute(UUID uniqueId) {
        MuteEntry removed = storage.remove(uniqueId);
        if (removed != null) {
            storage.save();
            return true;
        }
        return false;
    }

    public Optional<MuteEntry> findActiveMute(UUID uniqueId) {
        MuteEntry entry = storage.get(uniqueId);
        if (entry == null) {
            return Optional.empty();
        }

        if (entry.isExpired(System.currentTimeMillis())) {
            storage.remove(uniqueId);
            storage.save();
            return Optional.empty();
        }

        return Optional.of(entry);
    }

    public Optional<MuteEntry> findMuteByQuery(String input) {
        UUID uniqueId = tryParseUuid(input);
        if (uniqueId != null) {
            return findActiveMute(uniqueId);
        }

        for (MuteEntry entry : storage.getAll()) {
            if (entry.getPlayerName().equalsIgnoreCase(input)) {
                if (entry.isExpired(System.currentTimeMillis())) {
                    storage.remove(entry.getUniqueId());
                    storage.save();
                    return Optional.empty();
                }
                return Optional.of(entry);
            }
        }

        return Optional.empty();
    }

    public int cleanupExpiredMutes() {
        long now = System.currentTimeMillis();
        List<UUID> expired = new ArrayList<>();
        Collection<MuteEntry> entries = storage.getAll();
        for (MuteEntry entry : entries) {
            if (entry.isExpired(now)) {
                expired.add(entry.getUniqueId());
            }
        }

        if (expired.isEmpty()) {
            return 0;
        }

        for (UUID uniqueId : expired) {
            storage.remove(uniqueId);
        }
        storage.save();
        return expired.size();
    }

    public List<MuteEntry> getActiveMutes() {
        cleanupExpiredMutes();
        List<MuteEntry> entries = new ArrayList<>(storage.getAll());
        entries.sort(Comparator.comparingLong(MuteEntry::getCreatedAtMillis).reversed());
        return entries;
    }

    public List<MuteEntry> getActiveMutesByReason(String reasonKey) {
        List<MuteEntry> filtered = new ArrayList<>();
        for (MuteEntry entry : getActiveMutes()) {
            if (entry.getReasonKey().equalsIgnoreCase(reasonKey)) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    public String formatCreatedAt(MuteEntry entry) {
        return formatInstant(entry.getCreatedAtMillis());
    }

    public String formatExpiresAt(MuteEntry entry) {
        return formatInstant(entry.getExpiresAtMillis());
    }

    public String formatRemaining(MuteEntry entry) {
        return DurationParser.formatDuration(entry.getExpiresAtMillis() - System.currentTimeMillis());
    }

    public List<String> buildMuteLines(MuteEntry entry) {
        MuteReason reason = MuteReason.get(entry.getReasonKey());
        if (reason == null) {
            return List.of("\u00A7cYou are muted.");
        }
        return reason.render(formatRemaining(entry), entry.getMuteId());
    }

    public void sendMuteMessage(Player player, MuteEntry entry) {
        for (String line : buildMuteLines(entry)) {
            player.sendMessage(line);
        }
    }

    public boolean sendMuteNotificationIfPending(Player player, MuteEntry entry) {
        synchronized (entry) {
            if (entry.isNotificationSent()) {
                return false;
            }
            sendMuteMessage(player, entry);
            entry.markNotificationSent();
        }
        storage.save();
        return true;
    }

    public String describeMute(MuteEntry entry) {
        return "player=" + entry.getPlayerName()
                + ", uuid=" + entry.getUniqueId()
                + ", reasonKey=" + entry.getReasonKey()
                + ", actor=" + entry.getActor()
                + ", muteId=" + entry.getMuteId()
                + ", createdAt=" + formatCreatedAt(entry)
                + ", expiresAt=" + formatExpiresAt(entry)
                + ", remaining=" + formatRemaining(entry);
    }

    public Optional<String> resolveKnownPlayerName(UUID uniqueId) {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(uniqueId);
        return Optional.ofNullable(player.getName());
    }

    private String formatInstant(long millis) {
        return dateFormatter.format(Instant.ofEpochMilli(millis));
    }

    private UUID tryParseUuid(String input) {
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String generateMuteId() {
        StringBuilder builder = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            builder.append(MUTE_ID_CHARSET[random.nextInt(MUTE_ID_CHARSET.length)]);
        }
        return builder.toString();
    }
}
