package com.andyoctopus.spfeastbans.command;

import com.andyoctopus.spfeastbans.BanTemplate;
import com.andyoctopus.spfeastbans.BanTemplates;
import com.andyoctopus.spfeastbans.SpfeastBansPlugin;
import com.andyoctopus.spfeastbans.ban.BanEntry;
import com.andyoctopus.spfeastbans.ban.BanService;
import com.andyoctopus.spfeastbans.util.CommandMessages;
import com.andyoctopus.spfeastbans.util.DurationParser;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class BanCommand implements TabExecutor {
    private final SpfeastBansPlugin plugin;
    private final boolean temporary;

    public BanCommand(SpfeastBansPlugin plugin, boolean temporary) {
        this.plugin = plugin;
        this.temporary = temporary;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        BanService banService = plugin.getBanService();

        if (temporary) {
            if (args.length < 3) {
                CommandMessages.sendUsage(sender, label, "[Name] [Template] [Duration] [Reason]");
                return true;
            }
        } else if (args.length < 2) {
            CommandMessages.sendUsage(sender, label, "[Name] [Template] [Reason]");
            return true;
        }

        Optional<BanService.ResolvedTarget> targetOptional = banService.resolveTarget(args[0]);
        if (targetOptional.isEmpty()) {
            CommandMessages.sendFailure(sender, "Target not found. Use an online player, a known offline player, or a UUID.");
            return true;
        }

        BanTemplate template = BanTemplates.get(args[1]);
        if (template == null) {
            CommandMessages.sendFailure(sender, "Unknown template. Available: " + String.join(", ", BanTemplates.getKeys()));
            return true;
        }

        BanService.ResolvedTarget target = targetOptional.get();
        long expiresAtMillis = -1L;
        int reasonIndex = 2;
        if (temporary) {
            Long parsedDuration = DurationParser.parseDurationMillis(args[2]);
            if (parsedDuration == null || parsedDuration <= 0L) {
                CommandMessages.sendFailure(sender, "Invalid duration. Example: 30d12h, 7d, 12h30m, 45m, 10s");
                return true;
            }
            expiresAtMillis = System.currentTimeMillis() + parsedDuration;
            reasonIndex = 3;
        }

        String reason = banService.resolveReason(template, collectReason(args, reasonIndex));
        BanEntry entry = banService.ban(target.uniqueId(), target.playerName(), args[1], reason, sender.getName(), expiresAtMillis);
        CommandMessages.broadcastPublicRemoval(entry.getPlayerName(), sender);
        CommandMessages.sendStaffBanSummary(sender, banService, entry);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            List<String> matches = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (player.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    matches.add(player.getName());
                }
            });
            return matches;
        }
        if (args.length == 2) {
            return partialMatches(args[1], BanTemplates.getKeys());
        }
        return Collections.emptyList();
    }

    private List<String> partialMatches(String input, List<String> candidates) {
        String prefix = input.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String candidate : candidates) {
            if (candidate.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                matches.add(candidate);
            }
        }
        return matches;
    }

    private String collectReason(String[] args, int startIndex) {
        if (startIndex >= args.length) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(args[i]);
        }
        return builder.toString();
    }
}

