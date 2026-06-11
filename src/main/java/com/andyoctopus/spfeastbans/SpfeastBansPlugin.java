package com.andyoctopus.spfeastbans;

import com.andyoctopus.spfeastbans.ban.BanService;
import com.andyoctopus.spfeastbans.ban.BanHistoryStorage;
import com.andyoctopus.spfeastbans.ban.BanStorage;
import com.andyoctopus.spfeastbans.command.BanCommand;
import com.andyoctopus.spfeastbans.command.BanInfoCommand;
import com.andyoctopus.spfeastbans.command.BanListCommand;
import com.andyoctopus.spfeastbans.command.HistoryCommand;
import com.andyoctopus.spfeastbans.command.SpfeastBansCommand;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
        getConfig().options().copyDefaults(true);
        saveConfig();

        File realtimeDir = new File(getDataFolder(), "realtime");
        File historyDir = new File(getDataFolder(), "history");
        ensureDirectory(realtimeDir);
        ensureDirectory(historyDir);

        File bansFile = new File(realtimeDir, "bans.yml");
        migrateLegacyFile("bans.yml", bansFile);
        this.banStorage = new BanStorage(bansFile);
        this.banStorage.load();
        File bansHistoryFile = new File(historyDir, "bans-history.yml");
        migrateLegacyFile("bans-history.yml", bansHistoryFile);
        this.banHistoryStorage = new BanHistoryStorage(bansHistoryFile);
        this.banHistoryStorage.load();
        this.banService = new BanService(this, banStorage, banHistoryStorage);
        File mutesFile = new File(realtimeDir, "mutes.yml");
        migrateLegacyFile("mutes.yml", mutesFile);
        this.muteStorage = new MuteStorage(mutesFile);
        this.muteStorage.load();
        File mutesHistoryFile = new File(historyDir, "mutes-history.yml");
        migrateLegacyFile("mutes-history.yml", mutesHistoryFile);
        this.muteHistoryStorage = new MuteHistoryStorage(mutesHistoryFile);
        this.muteHistoryStorage.load();
        this.muteService = new MuteService(this, muteStorage, muteHistoryStorage);

        registerCommands();
        getServer().getPluginManager().registerEvents(new PlayerLoginListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        scheduleCleanupTasks();

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

    public void reloadPlugin() {
        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        if (banStorage != null) {
            banStorage.load();
        }
        if (banHistoryStorage != null) {
            banHistoryStorage.load();
        }
        if (muteStorage != null) {
            muteStorage.load();
        }
        if (muteHistoryStorage != null) {
            muteHistoryStorage.load();
        }

        this.banService = new BanService(this, banStorage, banHistoryStorage);
        this.muteService = new MuteService(this, muteStorage, muteHistoryStorage);
        getServer().getScheduler().cancelTasks(this);
        scheduleCleanupTasks();
    }

    private void registerCommands() {
        BanCommand banCommand = new BanCommand(this, false);
        BanCommand tempBanCommand = new BanCommand(this, true);
        UnbanCommand unbanCommand = new UnbanCommand(this);
        TempMuteCommand tempMuteCommand = new TempMuteCommand(this);
        UnmuteCommand unmuteCommand = new UnmuteCommand(this);
        BanInfoCommand banInfoCommand = new BanInfoCommand(this);
        BanListCommand banListCommand = new BanListCommand(this);
        HistoryCommand historyCommand = new HistoryCommand(this);
        SpfeastBansCommand spfeastBansCommand = new SpfeastBansCommand(this);

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
        requireCommand("history").setExecutor(historyCommand);
        requireCommand("history").setTabCompleter(historyCommand);
        requireCommand("spfeastbans").setExecutor(spfeastBansCommand);
        requireCommand("spfeastbans").setTabCompleter(spfeastBansCommand);
    }

    private PluginCommand requireCommand(String name) {
        return Objects.requireNonNull(getCommand(name), () -> "Missing command in plugin.yml: " + name);
    }

    private void scheduleCleanupTasks() {
        getServer().getScheduler().runTaskTimer(this, () -> {
            if (banService != null) {
                banService.cleanupExpiredBans();
            }
        }, 20L * 60L, 20L * 300L);
        getServer().getScheduler().runTaskTimer(this, () -> {
            if (muteService != null) {
                muteService.cleanupExpiredMutes();
            }
        }, 20L * 60L, 20L * 300L);
    }

    private void ensureDirectory(File directory) {
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    private void migrateLegacyFile(String legacyName, File target) {
        File legacyFile = new File(getDataFolder(), legacyName);
        if (!legacyFile.exists() || target.exists()) {
            return;
        }

        File parent = target.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try {
            Files.move(legacyFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            getLogger().warning("Failed to migrate legacy file " + legacyFile.getName() + " to " + target.getPath() + ": " + exception.getMessage());
        }
    }
}
