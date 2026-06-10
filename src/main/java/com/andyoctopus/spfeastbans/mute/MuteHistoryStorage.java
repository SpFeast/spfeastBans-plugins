package com.andyoctopus.spfeastbans.mute;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MuteHistoryStorage {
    private final File file;
    private final Map<String, MuteEntry> history = new LinkedHashMap<>();
    private Logger logger;

    public MuteHistoryStorage(File file) {
        this.file = file;
    }

    public synchronized void setLogger(Logger logger) {
        this.logger = logger;
    }

    public synchronized void load() {
        history.clear();

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
                history.put(key, MuteEntry.fromHistory(section));
            } catch (IllegalArgumentException exception) {
                log(Level.WARNING, "Skipping invalid mute history record: " + key, exception);
            }
        }
    }

    public synchronized void save() {
        YamlConfiguration configuration = new YamlConfiguration();
        for (Map.Entry<String, MuteEntry> entry : history.entrySet()) {
            ConfigurationSection section = configuration.createSection(entry.getKey());
            entry.getValue().writeToHistory(section);
        }

        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            configuration.save(file);
        } catch (IOException exception) {
            log(Level.SEVERE, "Failed to save mutes-history.yml", exception);
        }
    }

    public synchronized void append(MuteEntry entry) {
        history.put(entry.getCreatedAtMillis() + "-" + entry.getMuteId(), entry);
    }

    public synchronized Collection<MuteEntry> getAll() {
        return new ArrayList<>(history.values());
    }

    private void log(Level level, String message, Throwable throwable) {
        if (logger != null) {
            logger.log(level, message, throwable);
        }
    }
}
