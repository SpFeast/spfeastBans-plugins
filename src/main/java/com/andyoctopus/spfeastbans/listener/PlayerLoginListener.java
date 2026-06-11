package com.andyoctopus.spfeastbans.listener;

import com.andyoctopus.spfeastbans.SpfeastBansPlugin;
import com.andyoctopus.spfeastbans.ban.BanEntry;
import com.andyoctopus.spfeastbans.ban.BanService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.Optional;

public final class PlayerLoginListener implements Listener {
    private final SpfeastBansPlugin plugin;

    public PlayerLoginListener(SpfeastBansPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        BanService banService = plugin.getBanService();
        Optional<BanEntry> entryOptional = banService.resolveBanForLogin(event.getPlayer().getUniqueId());
        if (entryOptional.isEmpty()) {
            return;
        }

        event.disallow(PlayerLoginEvent.Result.KICK_BANNED, banService.buildKickMessage(entryOptional.get()));
    }
}
