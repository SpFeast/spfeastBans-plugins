package com.andyoctopus.spfeastapi;

import com.andyoctopus.spfeastbans.SpfeastBansPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpfeastApiPlugin extends JavaPlugin {
    private SpfeastApi service;

    @Override
    public void onEnable() {
        Plugin plugin = getServer().getPluginManager().getPlugin("spfeastBans");
        if (!(plugin instanceof SpfeastBansPlugin bansPlugin)) {
            getLogger().severe("spfeastBans not found, disabling spfeastApi.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.service = new SpfeastApiService(bansPlugin);
        getServer().getServicesManager().register(SpfeastApi.class, service, this, ServicePriority.Normal);
        getLogger().info("spfeastApi enabled.");
    }

    @Override
    public void onDisable() {
        getServer().getServicesManager().unregisterAll(this);
    }

    public SpfeastApi getApi() {
        return service;
    }
}
