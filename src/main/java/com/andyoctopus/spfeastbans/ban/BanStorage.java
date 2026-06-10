package com.andyoctopus.spfeastbans.ban;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class BanStorage {
    private final File file;
    private final Map<UUID, BanEntry> bans = new HashMap<>();
    private Logger logger;

    public BanStorage(File file) {
        this.file = file;
    }

    public synchronized void setLogger(Logger logger) {
        this.logger = logger;
    }

    public synchronized void load() {
        bans.clear();

        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            save();
            return;
        }

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        for (String key : configuration.getKeys(false)) {
            ConfigurationSection section = configuration.getConfigurationSection(key);
            if (section == null) {
                continue;
            }

            try {
                BanEntry entry = BanEntry.from(section);
                bans.put(entry.getUniqueId(), entry);
            } catch (IllegalArgumentException exception) {
                log(Level.WARNING, "Skipping invalid ban record: " + key, exception);
            }
        }
    }

    public synchronized void save() {
        YamlConfiguration configuration = new YamlConfiguration();
        for (BanEntry entry : bans.values()) {
            ConfigurationSection section = configuration.createSection(entry.getUniqueId().toString());
            entry.writeTo(section);
        }

        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            configuration.save(file);
        } catch (IOException exception) {
            log(Level.SEVERE, "Failed to save bans.yml", exception);
        }
    }

    public synchronized BanEntry get(UUID uniqueId) {
        return bans.get(uniqueId);
    }

    public synchronized void put(BanEntry entry) {
        bans.put(entry.getUniqueId(), entry);
    }

    public synchronized BanEntry remove(UUID uniqueId) {
        return bans.remove(uniqueId);
    }

    public synchronized Collection<BanEntry> getAll() {
        return new ArrayList<>(bans.values());
    }

    private void log(Level level, String message, Throwable throwable) {
        if (logger != null) {
            logger.log(level, message, throwable);
        }
    }
}

