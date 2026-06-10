package com.andyoctopus.spfeastbans.ban;

import com.andyoctopus.spfeastbans.BanTemplate;
import com.andyoctopus.spfeastbans.BanTemplates;
import com.andyoctopus.spfeastbans.SpfeastBansPlugin;
import com.andyoctopus.spfeastbans.util.DurationParser;
import org.bukkit.Bukkit;
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

public final class BanService {
    private static final char[] BAN_ID_CHARSET = "ABCDEF123456789".toCharArray();

    private final SpfeastBansPlugin plugin;
    private final BanStorage storage;
    private final BanHistoryStorage historyStorage;
    private final DateTimeFormatter dateFormatter;
    private final Random random = new SecureRandom();

    public BanService(SpfeastBansPlugin plugin, BanStorage storage, BanHistoryStorage historyStorage) {
        this.plugin = plugin;
        this.storage = storage;
        this.historyStorage = historyStorage;
        this.storage.setLogger(plugin.getLogger());
        this.historyStorage.setLogger(plugin.getLogger());
        this.dateFormatter = DateTimeFormatter.ofPattern(plugin.getConfig().getString("date-format", "yyyy-MM-dd HH:mm:ss"))
                .withZone(ZoneId.systemDefault());
        cleanupExpiredBans();
    }

    public BanEntry ban(UUID uniqueId, String playerName, String templateKey, String reason, String actor, long expiresAtMillis) {
        long now = System.currentTimeMillis();
        Player onlinePlayer = Bukkit.getPlayer(uniqueId);
        boolean online = onlinePlayer != null && onlinePlayer.isOnline();
        boolean temporary = expiresAtMillis >= 0L;
        long originalDurationMillis = temporary ? Math.max(0L, expiresAtMillis - now) : -1L;
        boolean pendingActivation = temporary && !online;
        BanEntry entry = new BanEntry(
                uniqueId,
                playerName,
                templateKey,
                reason,
                actor,
                generateBanId(),
                generateNumericId(),
                now,
                expiresAtMillis,
                originalDurationMillis,
                pendingActivation
        );
        storage.put(entry);
        storage.save();
        historyStorage.append(entry);
        historyStorage.save();
        kickIfOnline(entry);
        return entry;
    }

    public boolean unban(UUID uniqueId) {
        BanEntry removed = storage.remove(uniqueId);
        if (removed != null) {
            storage.save();
            return true;
        }
        return false;
    }

    public Optional<BanEntry> findActiveBan(UUID uniqueId) {
        BanEntry entry = storage.get(uniqueId);
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

    public Optional<BanEntry> resolveBanForLogin(UUID uniqueId) {
        BanEntry entry = storage.get(uniqueId);
        if (entry == null) {
            return Optional.empty();
        }

        long now = System.currentTimeMillis();
        if (entry.isPendingActivation()) {
            entry = entry.activate(now);
            storage.put(entry);
            storage.save();
            return Optional.of(entry);
        }

        if (entry.isExpired(now)) {
            storage.remove(uniqueId);
            storage.save();
            return Optional.empty();
        }

        return Optional.of(entry);
    }

    public Optional<BanEntry> findBanByQuery(String input) {
        UUID uniqueId = tryParseUuid(input);
        if (uniqueId != null) {
            return findActiveBan(uniqueId);
        }

        for (BanEntry entry : storage.getAll()) {
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

    public Optional<ResolvedTarget> resolveTarget(String input) {
        Player online = Bukkit.getPlayerExact(input);
        if (online != null) {
            return Optional.of(new ResolvedTarget(online.getUniqueId(), online.getName()));
        }

        OfflinePlayer cached = Bukkit.getOfflinePlayerIfCached(input);
        if (cached != null && (cached.getName() != null || cached.hasPlayedBefore())) {
            return Optional.of(new ResolvedTarget(cached.getUniqueId(), cached.getName() == null ? input : cached.getName()));
        }

        UUID uniqueId = tryParseUuid(input);
        if (uniqueId != null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uniqueId);
            String playerName = offlinePlayer.getName() == null ? uniqueId.toString() : offlinePlayer.getName();
            return Optional.of(new ResolvedTarget(uniqueId, playerName));
        }

        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(input)) {
                return Optional.of(new ResolvedTarget(offlinePlayer.getUniqueId(), offlinePlayer.getName()));
            }
        }

        return Optional.empty();
    }

    public int cleanupExpiredBans() {
        long now = System.currentTimeMillis();
        List<UUID> expired = new ArrayList<>();
        Collection<BanEntry> entries = storage.getAll();
        for (BanEntry entry : entries) {
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

    public int getActiveBanCount() {
        cleanupExpiredBans();
        int count = 0;
        for (BanEntry entry : storage.getAll()) {
            if (!entry.isPendingActivation()) {
                count++;
            }
        }
        return count;
    }

    public List<BanEntry> getActiveBans() {
        cleanupExpiredBans();
        List<BanEntry> entries = new ArrayList<>();
        for (BanEntry entry : storage.getAll()) {
            if (!entry.isPendingActivation()) {
                entries.add(entry);
            }
        }
        entries.sort(Comparator.comparingLong(BanEntry::getCreatedAtMillis).reversed());
        return entries;
    }

    public List<BanEntry> getActiveBansByTemplate(String templateKey) {
        List<BanEntry> filtered = new ArrayList<>();
        for (BanEntry entry : getActiveBans()) {
            if (entry.getTemplateKey().equalsIgnoreCase(templateKey)) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    public List<BanEntry> getBanHistory() {
        List<BanEntry> entries = new ArrayList<>(historyStorage.getAll());
        entries.sort(Comparator.comparingLong(BanEntry::getCreatedAtMillis).reversed());
        return entries;
    }

    public String buildKickMessage(BanEntry entry) {
        BanTemplate template = BanTemplates.get(entry.getTemplateKey());
        if (template == null) {
            return "\u00A7cYou are banned from this server.";
        }

        String durationText = "";
        if (template.usesDurationPlaceholder()) {
            if (entry.isPermanent()) {
                durationText = plugin.getConfig().getString("permanent-text", "permanent");
            } else {
                durationText = DurationParser.formatDuration(Math.max(0L, entry.getExpiresAtMillis() - System.currentTimeMillis()));
            }
        }

        List<String> rendered = template.render(
                durationText,
                entry.getReason(),
                entry.getBanId(),
                entry.getNumericId(),
                formatInstant(entry.getCreatedAtMillis())
        );
        return String.join("\n", rendered);
    }

    public String formatCreatedAt(BanEntry entry) {
        return formatInstant(entry.getCreatedAtMillis());
    }

    public String formatExpiresAt(BanEntry entry) {
        if (entry.isPendingActivation()) {
            return "starts on next login";
        }
        if (entry.isPermanent()) {
            return plugin.getConfig().getString("permanent-text", "permanent");
        }
        return formatInstant(entry.getExpiresAtMillis());
    }

    public String formatRemaining(BanEntry entry) {
        if (entry.isPendingActivation()) {
            return DurationParser.formatDuration(entry.getOriginalDurationMillis());
        }
        if (entry.isPermanent()) {
            return plugin.getConfig().getString("permanent-text", "permanent");
        }
        return DurationParser.formatDuration(entry.getExpiresAtMillis() - System.currentTimeMillis());
    }

    public String describeBan(BanEntry entry) {
        String expires = formatExpiresAt(entry);
        String remaining = formatRemaining(entry);
        return "player=" + entry.getPlayerName()
                + ", uuid=" + entry.getUniqueId()
                + ", template=" + entry.getTemplateKey()
                + ", reason=" + entry.getReason()
                + ", actor=" + entry.getActor()
                + ", banId=" + entry.getBanId()
                + ", createdAt=" + formatCreatedAt(entry)
                + ", expiresAt=" + expires
                + ", remaining=" + remaining;
    }

    public String resolveReason(BanTemplate template, String rawReason) {
        if (rawReason == null || rawReason.isBlank()) {
            if (template.usesReasonPlaceholder()) {
                return plugin.getConfig().getString("default-reason-fallback", "No reason provided");
            }
            return "";
        }
        return rawReason;
    }

    private void kickIfOnline(BanEntry entry) {
        Player player = Bukkit.getPlayer(entry.getUniqueId());
        if (player != null && player.isOnline()) {
            player.kickPlayer(buildKickMessage(entry));
        }
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

    private String generateBanId() {
        StringBuilder builder = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            builder.append(BAN_ID_CHARSET[random.nextInt(BAN_ID_CHARSET.length)]);
        }
        return builder.toString();
    }

    private String generateNumericId() {
        int value = 100000 + random.nextInt(900000);
        return Integer.toString(value);
    }

    public record ResolvedTarget(UUID uniqueId, String playerName) {
    }
}
