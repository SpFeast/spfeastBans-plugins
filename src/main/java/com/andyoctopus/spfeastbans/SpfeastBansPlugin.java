package com.andyoctopus.spfeastbans;

import com.andyoctopus.spfeastbans.ban.BanService;
import com.andyoctopus.spfeastbans.ban.BanHistoryStorage;
import com.andyoctopus.spfeastbans.ban.BanStorage;
import com.andyoctopus.spfeastbans.command.BanCommand;
import com.andyoctopus.spfeastbans.command.BanInfoCommand;
import com.andyoctopus.spfeastbans.command.BanListCommand;
import com.andyoctopus.spfeastbans.command.TempMuteCommand;
import com.andyoctopus.spfeastbans.command.UnbanCommand;
import com.andyoctopus.spfeastbans.command.UnmuteCommand;
import com.andyoctopus.spfeastbans.listener.PlayerChatListener;
import com.andyoctopus.spfeastbans.listener.PlayerJoinListener;
import com.andyoctopus.spfeastbans.listener.PlayerLoginListener;
import com.andyoctopus.spfeastbans.mute.MuteService;
import com.andyoctopus.spfeastbans.mute.MuteHistoryStorage;
import com.andyoctopus.spfeastbans.mute.MuteStorage;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public final class SpfeastBansPlugin extends JavaPlugin {
    private BanStorage banStorage;
    private BanHistoryStorage banHistoryStorage;
    private BanService banService;
    private MuteStorage muteStorage;
    private MuteHistoryStorage muteHistoryStorage;
    private MuteService muteService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.banStorage = new BanStorage(new File(getDataFolder(), "bans.yml"));
        this.banStorage.load();
        this.banHistoryStorage = new BanHistoryStorage(new File(getDataFolder(), "bans-history.yml"));
        this.banHistoryStorage.load();
        this.banService = new BanService(this, banStorage, banHistoryStorage);
        this.muteStorage = new MuteStorage(new File(getDataFolder(), "mutes.yml"));
        this.muteStorage.load();
        this.muteHistoryStorage = new MuteHistoryStorage(new File(getDataFolder(), "mutes-history.yml"));
        this.muteHistoryStorage.load();
        this.muteService = new MuteService(this, muteStorage, muteHistoryStorage);

        registerCommands();
        getServer().getPluginManager().registerEvents(new PlayerLoginListener(banService), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this, muteService), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, muteService), this);
        getServer().getScheduler().runTaskTimer(this, banService::cleanupExpiredBans, 20L * 60L, 20L * 300L);
        getServer().getScheduler().runTaskTimer(this, muteService::cleanupExpiredMutes, 20L * 60L, 20L * 300L);

        getLogger().info("spfeastBans enabled. Active bans: " + banService.getActiveBanCount());
    }

    @Override
    public void onDisable() {
        if (banStorage != null) {
            banStorage.save();
        }
        if (banHistoryStorage != null) {
            banHistoryStorage.save();
        }
        if (muteStorage != null) {
            muteStorage.save();
        }
        if (muteHistoryStorage != null) {
            muteHistoryStorage.save();
        }
    }

    public BanService getBanService() {
        return banService;
    }

    public MuteService getMuteService() {
        return muteService;
    }

    private void registerCommands() {
        BanCommand banCommand = new BanCommand(this, false);
        BanCommand tempBanCommand = new BanCommand(this, true);
        UnbanCommand unbanCommand = new UnbanCommand(this);
        TempMuteCommand tempMuteCommand = new TempMuteCommand(this);
        UnmuteCommand unmuteCommand = new UnmuteCommand(this);
        BanInfoCommand banInfoCommand = new BanInfoCommand(this);
        BanListCommand banListCommand = new BanListCommand(this);

        requireCommand("ban").setExecutor(banCommand);
        requireCommand("ban").setTabCompleter(banCommand);
        requireCommand("tempban").setExecutor(tempBanCommand);
        requireCommand("tempban").setTabCompleter(tempBanCommand);
        requireCommand("unban").setExecutor(unbanCommand);
        requireCommand("unban").setTabCompleter(unbanCommand);
        requireCommand("tempmute").setExecutor(tempMuteCommand);
        requireCommand("tempmute").setTabCompleter(tempMuteCommand);
        requireCommand("unmute").setExecutor(unmuteCommand);
        requireCommand("unmute").setTabCompleter(unmuteCommand);
        requireCommand("baninfo").setExecutor(banInfoCommand);
        requireCommand("baninfo").setTabCompleter(banInfoCommand);
        requireCommand("banlist").setExecutor(banListCommand);
        requireCommand("banlist").setTabCompleter(banListCommand);
    }

    private PluginCommand requireCommand(String name) {
        return Objects.requireNonNull(getCommand(name), () -> "Missing command in plugin.yml: " + name);
    }
}

