package com.andyoctopus.spfeastbans.listener;

import com.andyoctopus.spfeastbans.SpfeastBansPlugin;
import com.andyoctopus.spfeastbans.mute.MuteEntry;
import com.andyoctopus.spfeastbans.mute.MuteService;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Optional;

public final class PlayerChatListener implements Listener {
    private final SpfeastBansPlugin plugin;
    private final MuteService muteService;

    public PlayerChatListener(SpfeastBansPlugin plugin, MuteService muteService) {
        this.plugin = plugin;
        this.muteService = muteService;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncChatEvent event) {
        Optional<MuteEntry> entryOptional = muteService.findActiveMute(event.getPlayer().getUniqueId());
        if (entryOptional.isEmpty()) {
            return;
        }

        MuteEntry entry = entryOptional.get();
        event.setCancelled(true);
        Bukkit.getScheduler().runTask(plugin, () -> muteService.sendMuteNotificationIfPending(event.getPlayer(), entry));
    }
}
