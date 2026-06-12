package com.andyoctopus.spfeastbans.command;

import com.andyoctopus.spfeastbans.SpfeastBansPlugin;
import com.andyoctopus.spfeastbans.util.CommandMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class SpfeastBansCommand implements TabExecutor {
    private final SpfeastBansPlugin plugin;

    public SpfeastBansCommand(SpfeastBansPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
            CommandMessages.sendUsage(sender, label, "reload");
            return true;
        }

        plugin.reloadPlugin();
        CommandMessages.sendSuccess(sender, "spfeastBans reloaded.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            if ("reload".startsWith(prefix)) {
                return List.of("reload");
            }
        }
        return Collections.emptyList();
    }
}
