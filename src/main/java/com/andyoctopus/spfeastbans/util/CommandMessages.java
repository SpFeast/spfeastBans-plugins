package com.andyoctopus.spfeastbans.util;

import com.andyoctopus.spfeastbans.ban.BanEntry;
import com.andyoctopus.spfeastbans.ban.BanService;
import com.andyoctopus.spfeastbans.mute.MuteEntry;
import com.andyoctopus.spfeastbans.mute.MuteReason;
import com.andyoctopus.spfeastbans.mute.MuteService;
import com.andyoctopus.spfeastbans.punishment.PunishmentHistoryEntry;
import com.andyoctopus.spfeastbans.punishment.PunishmentListEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public final class CommandMessages {
    public static final String STAFF_VIEW_PERMISSION = "spfeastbans.staffview";
    private static final int BANLIST_PAGE_SIZE = 4;

    private CommandMessages() {
    }

    public static void sendUsage(CommandSender sender, String label, String placeholder) {
        sender.sendMessage(color("&e" + placeholder));
        sender.sendMessage(color("&7/" + label + " &f" + placeholder));
    }

    public static void sendFailure(CommandSender sender, String message) {
        sender.sendMessage(color("&c" + message));
    }

    public static void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage(color("&a" + message));
    }

    public static void sendStaffBanSummary(CommandSender sender, BanService banService, BanEntry entry) {
        sender.sendMessage(color("&cBanned &f" + entry.getPlayerName() + " &cwith template &e" + entry.getTemplateKey()));
        sender.sendMessage(color("&7Reason: &f" + safe(entry.getReason())));
        sender.sendMessage(color("&7Ban ID: &f#" + entry.getBanId() + " &8| &7Actor: &f" + entry.getActor()));
        sender.sendMessage(color("&7UUID: &f" + entry.getUniqueId()));
        sender.sendMessage(color("&7Created: &f" + banService.formatCreatedAt(entry)));
        sender.sendMessage(color("&7Expires: &f" + banService.formatExpiresAt(entry)));
        sender.sendMessage(color("&7Remaining: &f" + banService.formatRemaining(entry)));
    }

    public static void sendStaffMuteSummary(CommandSender sender, MuteService muteService, MuteEntry entry) {
        MuteReason reason = MuteReason.get(entry.getReasonKey());
        String summary = reason == null ? entry.getReasonKey() : reason.getStaffSummary();
        sender.sendMessage(color("&cMuted &f" + entry.getPlayerName() + " &cwith reason &e" + entry.getReasonKey()));
        sender.sendMessage(color("&7Reason: &f" + summary));
        sender.sendMessage(color("&7Mute ID: &f#" + entry.getMuteId() + " &8| &7Actor: &f" + entry.getActor()));
        sender.sendMessage(color("&7UUID: &f" + entry.getUniqueId()));
        sender.sendMessage(color("&7Created: &f" + muteService.formatCreatedAt(entry)));
        sender.sendMessage(color("&7Expires: &f" + muteService.formatExpiresAt(entry)));
        sender.sendMessage(color("&7Remaining: &f" + muteService.formatRemaining(entry)));
    }

    public static void sendBanInfo(CommandSender sender, BanService banService, BanEntry entry) {
        sender.sendMessage(color("&aCurrent ban information for &f" + entry.getPlayerName()));
        sender.sendMessage(color("&7Template: &f" + entry.getTemplateKey() + " &8| &7Actor: &f" + entry.getActor()));
        sender.sendMessage(color("&7Reason: &f" + safe(entry.getReason())));
        sender.sendMessage(color("&7Ban ID: &f#" + entry.getBanId()));
        sender.sendMessage(color("&7UUID: &f" + entry.getUniqueId()));
        sender.sendMessage(color("&7Created: &f" + banService.formatCreatedAt(entry)));
        sender.sendMessage(color("&7Expires: &f" + banService.formatExpiresAt(entry)));
        sender.sendMessage(color("&7Remaining: &f" + banService.formatRemaining(entry)));
    }

    public static void sendMuteInfo(CommandSender sender, MuteService muteService, MuteEntry entry) {
        MuteReason reason = MuteReason.get(entry.getReasonKey());
        String summary = reason == null ? entry.getReasonKey() : reason.getStaffSummary();
        sender.sendMessage(color("&aCurrent mute information for &f" + entry.getPlayerName()));
        sender.sendMessage(color("&7Reason Key: &f" + entry.getReasonKey() + " &8| &7Actor: &f" + entry.getActor()));
        sender.sendMessage(color("&7Reason: &f" + summary));
        sender.sendMessage(color("&7Mute ID: &f#" + entry.getMuteId()));
        sender.sendMessage(color("&7UUID: &f" + entry.getUniqueId()));
        sender.sendMessage(color("&7Created: &f" + muteService.formatCreatedAt(entry)));
        sender.sendMessage(color("&7Expires: &f" + muteService.formatExpiresAt(entry)));
        sender.sendMessage(color("&7Remaining: &f" + muteService.formatRemaining(entry)));
    }

    public static void sendUnbanSummary(CommandSender sender, BanEntry entry) {
        sender.sendMessage(color("&aUnbanned &f" + entry.getPlayerName()));
        sender.sendMessage(color("&7UUID: &f" + entry.getUniqueId()));
        sender.sendMessage(color("&7Template: &f" + entry.getTemplateKey()));
    }

    public static void sendUnmuteSummary(CommandSender sender, MuteEntry entry) {
        sender.sendMessage(color("&aUnmuted &f" + entry.getPlayerName()));
        sender.sendMessage(color("&7UUID: &f" + entry.getUniqueId()));
        sender.sendMessage(color("&7Reason Key: &f" + entry.getReasonKey()));
    }

    public static void sendBanList(CommandSender sender, List<PunishmentListEntry> entries, String filterLabel, int page, String permanentText) {
        if (entries.isEmpty()) {
            sendFailure(sender, "No active punishments found" + ("all".equalsIgnoreCase(filterLabel) ? "." : " for type " + filterLabel + "."));
            return;
        }

        int totalPages = (int) Math.ceil(entries.size() / (double) BANLIST_PAGE_SIZE);
        int safePage = Math.max(1, Math.min(page, totalPages));
        int start = (safePage - 1) * BANLIST_PAGE_SIZE;
        int end = Math.min(start + BANLIST_PAGE_SIZE, entries.size());

        String listCommandBase = "all".equalsIgnoreCase(filterLabel) ? "/banlist" : "/banlist " + filterLabel;

        sender.sendMessage(color("&9&m--------------------------------------------------"));
        sender.sendMessage(color("&6&lActive Punishments &8\u00BB &e" + filterLabel + " &7(Page &f" + safePage + "&7/&f" + totalPages + "&7)"));
        sender.sendMessage(color("&7Showing &f" + (start + 1) + "&7-&f" + end + " &7of &f" + entries.size() + " &7active entries"));
        sender.sendMessage(color("&9&m--------------------------------------------------"));
        for (int i = start; i < end; i++) {
            PunishmentListEntry entry = entries.get(i);
            int displayIndex = i + 1;
            sender.sendMessage(buildBanListHeader(entry, displayIndex));
            sender.sendMessage(color("&7  Remaining: &f" + formatRemainingForList(entry, permanentText) + " &8| &7By: &f" + entry.actor()));
            sender.sendMessage(color("&7  UUID: &f" + entry.uniqueId()));
            sender.sendMessage(buildBanListActions(entry));
        }

        if (safePage > 1) {
            sender.sendMessage(buildPagerButton("Previous Page", listCommandBase + " " + (safePage - 1), NamedTextColor.YELLOW));
        } else {
            sender.sendMessage(color("&8Previous Page &8\u00BB &7None"));
        }

        if (safePage < totalPages) {
            sender.sendMessage(buildPagerButton("Next Page", listCommandBase + " " + (safePage + 1), NamedTextColor.GREEN));
        } else {
            sender.sendMessage(color("&8Next Page &8\u00BB &7None"));
        }
        sender.sendMessage(color("&9&m--------------------------------------------------"));
    }

    public static int getBanListPageSize() {
        return BANLIST_PAGE_SIZE;
    }

    public static void sendPunishmentHistory(CommandSender sender, String playerName, List<PunishmentHistoryEntry> entries, int page) {
        int totalPages = (int) Math.ceil(entries.size() / (double) BANLIST_PAGE_SIZE);
        int safePage = Math.max(1, Math.min(page, totalPages));
        int start = (safePage - 1) * BANLIST_PAGE_SIZE;
        int end = Math.min(start + BANLIST_PAGE_SIZE, entries.size());
        String listCommandBase = "/history " + playerName;

        sender.sendMessage(color("&9&m--------------------------------------------------"));
        sender.sendMessage(color("&6&lPunishment History &8\u00BB &e" + playerName + " &7(Page &f" + safePage + "&7/&f" + totalPages + "&7)"));
        sender.sendMessage(color("&7Showing &f" + (start + 1) + "&7-&f" + end + " &7of &f" + entries.size() + " &7historical entries"));
        sender.sendMessage(color("&9&m--------------------------------------------------"));
        for (int i = start; i < end; i++) {
            PunishmentHistoryEntry entry = entries.get(i);
            int displayIndex = i + 1;
            sender.sendMessage(buildHistoryHeader(entry, displayIndex));
            sender.sendMessage(color("&7  Issued: &f" + entry.createdAtText() + " &8| &7By: &f" + entry.actor()));
            sender.sendMessage(color("&7  Expires: &f" + entry.expiresAtText()));
            sender.sendMessage(color("&7  Details: &f" + entry.detailText()));
            sender.sendMessage(color("&7  " + entry.referenceLabel() + ": &f#" + entry.referenceId() + " &8| &7UUID: &f" + entry.uniqueId()));
        }

        if (safePage > 1) {
            sender.sendMessage(buildPagerButton("Previous Page", listCommandBase + " " + (safePage - 1), NamedTextColor.YELLOW));
        } else {
            sender.sendMessage(color("&8Previous Page &8\u00BB &7None"));
        }

        if (safePage < totalPages) {
            sender.sendMessage(buildPagerButton("Next Page", listCommandBase + " " + (safePage + 1), NamedTextColor.GREEN));
        } else {
            sender.sendMessage(color("&8Next Page &8\u00BB &7None"));
        }
        sender.sendMessage(color("&9&m--------------------------------------------------"));
    }

    private static String rankColor(int index) {
        return switch (index % 4) {
            case 1 -> "&c&l";
            case 2 -> "&6&l";
            case 3 -> "&e&l";
            default -> "&a&l";
        };
    }

    private static Component buildBanListHeader(PunishmentListEntry entry, int displayIndex) {
        return Component.text()
                .append(Component.text("#" + displayIndex + " ", legacyColor(rankColor(displayIndex))).decoration(TextDecoration.BOLD, true))
                .append(Component.text(entry.playerName(), NamedTextColor.AQUA)
                        .clickEvent(ClickEvent.runCommand("/baninfo " + entry.playerName()))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to view /baninfo " + entry.playerName(), NamedTextColor.GRAY))))
                .append(Component.text(" [" + entry.listTag() + "]", NamedTextColor.DARK_GRAY))
                .build();
    }

    private static Component buildHistoryHeader(PunishmentHistoryEntry entry, int displayIndex) {
        return Component.text()
                .append(Component.text("#" + displayIndex + " ", legacyColor(rankColor(displayIndex))).decoration(TextDecoration.BOLD, true))
                .append(Component.text(entry.playerName(), NamedTextColor.AQUA)
                        .clickEvent(ClickEvent.runCommand("/history " + entry.playerName()))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to run /history " + entry.playerName(), NamedTextColor.GRAY))))
                .append(Component.text(" [" + entry.listTag() + "]", NamedTextColor.DARK_GRAY))
                .build();
    }

    private static Component buildBanListActions(PunishmentListEntry entry) {
        boolean isBan = entry.type() == PunishmentListEntry.Type.BAN;
        String playerName = entry.playerName();
        return Component.text()
                .append(Component.text("  " + entry.referenceLabel() + ": ", NamedTextColor.GRAY))
                .append(Component.text("#" + entry.referenceId(), NamedTextColor.WHITE))
                .append(Component.text("  ", NamedTextColor.GRAY))
                .append(actionButton("UNBAN", "/unban " + playerName, NamedTextColor.RED, isBan,
                        (isBan ? "Click to run " : "Click to fill ") + "/unban " + playerName))
                .append(Component.space())
                .append(actionButton("UNMUTE", "/unmute " + playerName, NamedTextColor.GOLD, !isBan,
                        (!isBan ? "Click to run " : "Click to fill ") + "/unmute " + playerName))
                .build();
    }

    private static Component buildPagerButton(String label, String command, NamedTextColor accent) {
        return Component.text()
                .append(Component.text(label + " ", accent))
                .append(Component.text(">> ", NamedTextColor.DARK_GRAY))
                .append(Component.text(command, NamedTextColor.WHITE)
                        .clickEvent(ClickEvent.runCommand(command))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to run " + command, NamedTextColor.GRAY))))
                .build();
    }

    private static Component actionButton(String label, String command, NamedTextColor color, boolean runCommand, String hoverText) {
        Component button = Component.text("[" + label + "]", color).decoration(TextDecoration.BOLD, true)
                .hoverEvent(HoverEvent.showText(Component.text(hoverText, NamedTextColor.GRAY)));
        return runCommand ? button.clickEvent(ClickEvent.runCommand(command)) : button.clickEvent(ClickEvent.suggestCommand(command));
    }

    private static String formatRemainingForList(PunishmentListEntry entry, String permanentText) {
        if (entry.isPermanent()) {
            return permanentText;
        }
        return DurationParser.formatDuration(entry.expiresAtMillis() - System.currentTimeMillis());
    }

    private static NamedTextColor legacyColor(String colorCode) {
        if (colorCode.contains("&c")) {
            return NamedTextColor.RED;
        }
        if (colorCode.contains("&6")) {
            return NamedTextColor.GOLD;
        }
        if (colorCode.contains("&e")) {
            return NamedTextColor.YELLOW;
        }
        return NamedTextColor.GREEN;
    }

    public static void broadcastPublicRemoval(String playerName, CommandSender actor) {
        List<String> lines = List.of(
                "&cA player has been removed from your game.",
                "&bUse /report to continue helping out this server!"
        );

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (actor instanceof Player actorPlayer && onlinePlayer.getUniqueId().equals(actorPlayer.getUniqueId())) {
                continue;
            }
            if (onlinePlayer.hasPermission(STAFF_VIEW_PERMISSION)) {
                continue;
            }
            for (String line : lines) {
                onlinePlayer.sendMessage(color(line));
            }
        }
    }

    private static String safe(String text) {
        return text == null || text.isBlank() ? "No reason provided" : text;
    }

    private static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
