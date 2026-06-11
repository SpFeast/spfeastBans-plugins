package com.andyoctopus.spfeastbans.listener;

import com.andyoctopus.spfeastbans.SpfeastBansPlugin;
import com.andyoctopus.spfeastbans.mute.MuteService;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerJoinListener implements Listener {
    private final SpfeastBansPlugin plugin;

    public PlayerJoinListener(SpfeastBansPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            MuteService muteService = plugin.getMuteService();
            muteService.findActiveMute(event.getPlayer().getUniqueId())
                    .ifPresent(entry -> muteService.sendMuteNotificationIfPending(event.getPlayer(), entry));
        });
    }
}
