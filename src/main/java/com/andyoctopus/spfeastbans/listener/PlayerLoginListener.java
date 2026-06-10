package com.andyoctopus.spfeastbans.listener;

import com.andyoctopus.spfeastbans.ban.BanEntry;
import com.andyoctopus.spfeastbans.ban.BanService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.Optional;

public final class PlayerLoginListener implements Listener {
    private final BanService banService;

    public PlayerLoginListener(BanService banService) {
        this.banService = banService;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Optional<BanEntry> entryOptional = banService.findActiveBan(event.getPlayer().getUniqueId());
        if (entryOptional.isEmpty()) {
            return;
        }

        event.disallow(PlayerLoginEvent.Result.KICK_BANNED, banService.buildKickMessage(entryOptional.get()));
    }
}

