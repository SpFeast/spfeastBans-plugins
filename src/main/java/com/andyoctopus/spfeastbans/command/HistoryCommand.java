package com.andyoctopus.spfeastbans.command;

import com.andyoctopus.spfeastbans.SpfeastBansPlugin;
import com.andyoctopus.spfeastbans.ban.BanEntry;
import com.andyoctopus.spfeastbans.ban.BanService;
import com.andyoctopus.spfeastbans.mute.MuteEntry;
import com.andyoctopus.spfeastbans.mute.MuteReason;
import com.andyoctopus.spfeastbans.mute.MuteService;
import com.andyoctopus.spfeastbans.punishment.PunishmentHistoryEntry;
import com.andyoctopus.spfeastbans.punishment.PunishmentListEntry;
import com.andyoctopus.spfeastbans.util.CommandMessages;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class HistoryCommand implements TabExecutor {
    private final SpfeastBansPlugin plugin;

    public HistoryCommand(SpfeastBansPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            CommandMessages.sendUsage(sender, label, "[Player]");
            return true;
        }

        BanService banService = plugin.getBanService();
        MuteService muteService = plugin.getMuteService();
        String query = args[0];
        Optional<BanService.ResolvedTarget> resolvedTarget = banService.resolveTarget(query);
        UUID resolvedUniqueId = resolvedTarget.map(BanService.ResolvedTarget::uniqueId).orElseGet(() -> tryParseUuid(query));
        String displayName = resolvedTarget.map(BanService.ResolvedTarget::playerName).orElse(query);

        List<PunishmentHistoryEntry> entries = new ArrayList<>();
        for (BanEntry entry : banService.getBanHistory()) {
            if (matches(entry.getUniqueId(), entry.getPlayerName(), resolvedUniqueId, query)) {
                entries.add(new PunishmentHistoryEntry(
                        PunishmentListEntry.Type.BAN,
                        entry.getUniqueId(),
                        entry.getPlayerName(),
                        entry.getTemplateKey(),
                        entry.getActor(),
                        entry.getBanId(),
                        entry.getCreatedAtMillis(),
                        banService.formatCreatedAt(entry),
                        banService.formatExpiresAt(entry),
                        safe(entry.getReason())
                ));
            }
        }
        for (MuteEntry entry : muteService.getMuteHistory()) {
            if (matches(entry.getUniqueId(), entry.getPlayerName(), resolvedUniqueId, query)) {
                MuteReason reason = MuteReason.get(entry.getReasonKey());
                entries.add(new PunishmentHistoryEntry(
                        PunishmentListEntry.Type.MUTE,
                        entry.getUniqueId(),
                        entry.getPlayerName(),
                        entry.getReasonKey(),
                        entry.getActor(),
                        entry.getMuteId(),
                        entry.getCreatedAtMillis(),
                        muteService.formatCreatedAt(entry),
                        muteService.formatExpiresAt(entry),
                        reason == null ? entry.getReasonKey() : reason.getStaffSummary()
                ));
            }
        }

        entries.sort(Comparator.comparingLong(PunishmentHistoryEntry::createdAtMillis).reversed());
        if (entries.isEmpty()) {
            CommandMessages.sendFailure(sender, "No punishment history found for " + query + ".");
            return true;
        }

        CommandMessages.sendPunishmentHistory(sender, displayName, entries);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }

        String prefix = args[0].toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                matches.add(player.getName());
            }
        }
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            String name = offlinePlayer.getName();
            if (name != null && name.toLowerCase(Locale.ROOT).startsWith(prefix) && !matches.contains(name)) {
                matches.add(name);
            }
        }
        return matches;
    }

    private boolean matches(UUID entryUniqueId, String entryPlayerName, UUID resolvedUniqueId, String query) {
        if (resolvedUniqueId != null) {
            return entryUniqueId.equals(resolvedUniqueId);
        }
        return entryPlayerName.equalsIgnoreCase(query);
    }

    private UUID tryParseUuid(String input) {
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String safe(String text) {
        return text == null || text.isBlank() ? "No reason provided" : text;
    }
}
