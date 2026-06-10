package com.andyoctopus.spfeastbans.command;

import com.andyoctopus.spfeastbans.SpfeastBansPlugin;
import com.andyoctopus.spfeastbans.ban.BanEntry;
import com.andyoctopus.spfeastbans.ban.BanService;
import com.andyoctopus.spfeastbans.mute.MuteEntry;
import com.andyoctopus.spfeastbans.mute.MuteService;
import com.andyoctopus.spfeastbans.util.CommandMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class BanInfoCommand implements TabExecutor {
    private final SpfeastBansPlugin plugin;

    public BanInfoCommand(SpfeastBansPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            CommandMessages.sendUsage(sender, label, "[Name/IP]");
            return true;
        }

        BanService banService = plugin.getBanService();
        MuteService muteService = plugin.getMuteService();
        Optional<BanEntry> banOptional = banService.findBanByQuery(args[0]);
        Optional<MuteEntry> muteOptional = muteService.findMuteByQuery(args[0]);
        if (banOptional.isEmpty() && muteOptional.isEmpty()) {
            CommandMessages.sendFailure(sender, "This target has no active punishment record.");
            return true;
        }

        banOptional.ifPresent(entry -> CommandMessages.sendBanInfo(sender, banService, entry));
        muteOptional.ifPresent(entry -> CommandMessages.sendMuteInfo(sender, muteService, entry));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}

