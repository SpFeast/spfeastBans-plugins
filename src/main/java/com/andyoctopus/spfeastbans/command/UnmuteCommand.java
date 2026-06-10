package com.andyoctopus.spfeastbans.command;

import com.andyoctopus.spfeastbans.SpfeastBansPlugin;
import com.andyoctopus.spfeastbans.mute.MuteEntry;
import com.andyoctopus.spfeastbans.mute.MuteService;
import com.andyoctopus.spfeastbans.util.CommandMessages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class UnmuteCommand implements TabExecutor {
    private final SpfeastBansPlugin plugin;

    public UnmuteCommand(SpfeastBansPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            CommandMessages.sendUsage(sender, label, "[Name/IP]");
            return true;
        }

        MuteService muteService = plugin.getMuteService();
        Optional<MuteEntry> entryOptional = muteService.findMuteByQuery(args[0]);
        if (entryOptional.isEmpty()) {
            CommandMessages.sendFailure(sender, "No active mute record found.");
            return true;
        }

        MuteEntry entry = entryOptional.get();
        if (muteService.unmute(entry.getUniqueId())) {
            CommandMessages.sendUnmuteSummary(sender, entry);
        } else {
            CommandMessages.sendFailure(sender, "Failed to unmute. The record may already be gone.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            List<String> matches = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    matches.add(player.getName());
                }
            }
            return matches;
        }
        return Collections.emptyList();
    }
}
