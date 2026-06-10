package com.andyoctopus.spfeastbans.command;

import com.andyoctopus.spfeastbans.SpfeastBansPlugin;
import com.andyoctopus.spfeastbans.ban.BanService;
import com.andyoctopus.spfeastbans.mute.MuteEntry;
import com.andyoctopus.spfeastbans.mute.MuteReason;
import com.andyoctopus.spfeastbans.mute.MuteService;
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

public final class TempMuteCommand implements TabExecutor {
    private final SpfeastBansPlugin plugin;

    public TempMuteCommand(SpfeastBansPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2 || args.length > 3) {
            CommandMessages.sendUsage(sender, label, "[Name] [ReasonKey] [Time]");
            sender.sendMessage("\u00A77Reasons: underreview, minorchat, majorchat");
            return true;
        }

        BanService banService = plugin.getBanService();
        MuteService muteService = plugin.getMuteService();
        Optional<BanService.ResolvedTarget> targetOptional = banService.resolveTarget(args[0]);
        if (targetOptional.isEmpty()) {
            CommandMessages.sendFailure(sender, "Target not found. Use an online player, a known offline player, or a UUID.");
            return true;
        }

        MuteReason reason = MuteReason.get(args[1]);
        if (reason == null) {
            CommandMessages.sendFailure(sender, "Unknown mute reason. Available: " + String.join(", ", MuteReason.getKeys()));
            return true;
        }

        long durationMillis = reason.getDefaultDurationMillis();
        if (args.length == 3) {
            Long parsedDuration = DurationParser.parseDurationMillis(args[2]);
            if (parsedDuration == null || parsedDuration <= 0L) {
                CommandMessages.sendFailure(sender, "Invalid duration. Example: 1d, 12h, 30m, 10s");
                return true;
            }
            durationMillis = parsedDuration;
        }

        BanService.ResolvedTarget target = targetOptional.get();
        long expiresAtMillis = System.currentTimeMillis() + durationMillis;
        MuteEntry entry = muteService.mute(target.uniqueId(), target.playerName(), reason, sender.getName(), expiresAtMillis);
        CommandMessages.sendStaffMuteSummary(sender, muteService, entry);
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
            return partialMatches(args[1], MuteReason.getKeys());
        }
        if (args.length == 3) {
            return partialMatches(args[2], List.of("1d", "7d", "12h", "30m"));
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
}
