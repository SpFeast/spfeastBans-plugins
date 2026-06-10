package com.andyoctopus.spfeastbans.command;

import com.andyoctopus.spfeastbans.BanTemplates;
import com.andyoctopus.spfeastbans.SpfeastBansPlugin;
import com.andyoctopus.spfeastbans.ban.BanEntry;
import com.andyoctopus.spfeastbans.ban.BanService;
import com.andyoctopus.spfeastbans.mute.MuteEntry;
import com.andyoctopus.spfeastbans.mute.MuteReason;
import com.andyoctopus.spfeastbans.punishment.PunishmentListEntry;
import com.andyoctopus.spfeastbans.util.CommandMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class BanListCommand implements TabExecutor {
    private final SpfeastBansPlugin plugin;

    public BanListCommand(SpfeastBansPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String filter = null;
        int page = 1;
        if (args.length >= 1) {
            if (isInteger(args[0])) {
                page = parsePage(args[0], 1);
            } else {
                filter = args[0].toLowerCase(Locale.ROOT);
                if (!isKnownFilter(filter)) {
                    CommandMessages.sendFailure(sender, "Unknown type. Available: " + String.join(", ", availableFilters()));
                    return true;
                }
            }
        }
        if (args.length >= 2) {
            if (filter == null) {
                CommandMessages.sendUsage(sender, label, "[Type] [Page]");
                sender.sendMessage("\u00A77You can also use /" + label + " [Page]");
                return true;
            }
            if (!isInteger(args[1])) {
                CommandMessages.sendFailure(sender, "Page must be a number.");
                return true;
            }
            page = parsePage(args[1], 1);
        }
        if (args.length > 2) {
            CommandMessages.sendUsage(sender, label, "[Type] [Page]");
            sender.sendMessage("\u00A77You can also use /" + label + " [Page]");
            return true;
        }

        List<PunishmentListEntry> entries = collectEntries(filter);
        String permanentText = plugin.getConfig().getString("permanent-text", "permanent");
        CommandMessages.sendBanList(sender, entries, filter == null ? "all" : filter, page, permanentText);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            List<String> matches = new ArrayList<>();
            if ("1".startsWith(prefix)) {
                matches.add("1");
            }
            for (String key : availableFilters()) {
                if (key.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    matches.add(key);
                }
            }
            return matches;
        }

        if (args.length == 2 && !isInteger(args[0]) && isKnownFilter(args[0])) {
            List<PunishmentListEntry> entries = collectEntries(args[0]);
            return pageSuggestions(args[1], entries.size());
        }

        return Collections.emptyList();
    }

    private List<String> pageSuggestions(String input, int totalEntries) {
        int totalPages = Math.max(1, (int) Math.ceil(totalEntries / (double) CommandMessages.getBanListPageSize()));
        String prefix = input.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (int page = 1; page <= totalPages; page++) {
            String value = Integer.toString(page);
            if (value.startsWith(prefix)) {
                matches.add(value);
            }
        }
        return matches;
    }

    private boolean isKnownFilter(String filter) {
        return "ban".equalsIgnoreCase(filter)
                || "mute".equalsIgnoreCase(filter)
                || BanTemplates.get(filter) != null
                || MuteReason.get(filter) != null;
    }

    private List<String> availableFilters() {
        List<String> filters = new ArrayList<>();
        filters.add("ban");
        filters.add("mute");
        filters.addAll(BanTemplates.getKeys());
        filters.addAll(MuteReason.getKeys());
        return filters;
    }

    private List<PunishmentListEntry> collectEntries(String filter) {
        String normalized = filter == null ? "all" : filter.toLowerCase(Locale.ROOT);
        List<PunishmentListEntry> entries = new ArrayList<>();
        if ("all".equals(normalized) || "ban".equals(normalized) || BanTemplates.get(normalized) != null) {
            List<BanEntry> bans = ("all".equals(normalized) || "ban".equals(normalized))
                    ? plugin.getBanService().getActiveBans()
                    : plugin.getBanService().getActiveBansByTemplate(normalized);
            for (BanEntry entry : bans) {
                entries.add(new PunishmentListEntry(
                        PunishmentListEntry.Type.BAN,
                        entry.getUniqueId(),
                        entry.getPlayerName(),
                        entry.getTemplateKey(),
                        entry.getActor(),
                        entry.getBanId(),
                        entry.getCreatedAtMillis(),
                        entry.getExpiresAtMillis()
                ));
            }
        }
        if ("all".equals(normalized) || "mute".equals(normalized) || MuteReason.get(normalized) != null) {
            List<MuteEntry> mutes = ("all".equals(normalized) || "mute".equals(normalized))
                    ? plugin.getMuteService().getActiveMutes()
                    : plugin.getMuteService().getActiveMutesByReason(normalized);
            for (MuteEntry entry : mutes) {
                entries.add(new PunishmentListEntry(
                        PunishmentListEntry.Type.MUTE,
                        entry.getUniqueId(),
                        entry.getPlayerName(),
                        entry.getReasonKey(),
                        entry.getActor(),
                        entry.getMuteId(),
                        entry.getCreatedAtMillis(),
                        entry.getExpiresAtMillis()
                ));
            }
        }
        entries.sort((left, right) -> Long.compare(right.createdAtMillis(), left.createdAtMillis()));
        return entries;
    }

    private boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private int parsePage(String input, int fallback) {
        try {
            return Math.max(1, Integer.parseInt(input));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
