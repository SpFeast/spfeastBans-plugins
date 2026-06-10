package com.andyoctopus.spfeastbans.mute;

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

public final class MuteStorage {
    private final File file;
    private final Map<UUID, MuteEntry> mutes = new HashMap<>();
    private Logger logger;

    public MuteStorage(File file) {
        this.file = file;
    }

    public synchronized void setLogger(Logger logger) {
        this.logger = logger;
    }

    public synchronized void load() {
        mutes.clear();

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
                MuteEntry entry = MuteEntry.from(section);
                mutes.put(entry.getUniqueId(), entry);
            } catch (IllegalArgumentException exception) {
                log(Level.WARNING, "Skipping invalid mute record: " + key, exception);
            }
        }
    }

    public synchronized void save() {
        YamlConfiguration configuration = new YamlConfiguration();
        for (MuteEntry entry : mutes.values()) {
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
            log(Level.SEVERE, "Failed to save mutes.yml", exception);
        }
    }

    public synchronized MuteEntry get(UUID uniqueId) {
        return mutes.get(uniqueId);
    }

    public synchronized void put(MuteEntry entry) {
        mutes.put(entry.getUniqueId(), entry);
    }

    public synchronized MuteEntry remove(UUID uniqueId) {
        return mutes.remove(uniqueId);
    }

    public synchronized Collection<MuteEntry> getAll() {
        return new ArrayList<>(mutes.values());
    }

    private void log(Level level, String message, Throwable throwable) {
        if (logger != null) {
            logger.log(level, message, throwable);
        }
    }
}
