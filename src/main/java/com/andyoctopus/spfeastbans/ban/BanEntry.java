package com.andyoctopus.spfeastbans.ban;

import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

public final class BanEntry {
    private final UUID uniqueId;
    private final String playerName;
    private final String templateKey;
    private final String reason;
    private final String actor;
    private final String banId;
    private final String numericId;
    private final long createdAtMillis;
    private final long expiresAtMillis;

    public BanEntry(UUID uniqueId,
                    String playerName,
                    String templateKey,
                    String reason,
                    String actor,
                    String banId,
                    String numericId,
                    long createdAtMillis,
                    long expiresAtMillis) {
        this.uniqueId = uniqueId;
        this.playerName = playerName;
        this.templateKey = templateKey;
        this.reason = reason;
        this.actor = actor;
        this.banId = banId;
        this.numericId = numericId;
        this.createdAtMillis = createdAtMillis;
        this.expiresAtMillis = expiresAtMillis;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getTemplateKey() {
        return templateKey;
    }

    public String getReason() {
        return reason;
    }

    public String getActor() {
        return actor;
    }

    public String getBanId() {
        return banId;
    }

    public String getNumericId() {
        return numericId;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }

    public long getExpiresAtMillis() {
        return expiresAtMillis;
    }

    public boolean isPermanent() {
        return expiresAtMillis < 0L;
    }

    public boolean isExpired(long now) {
        return !isPermanent() && expiresAtMillis <= now;
    }

    public void writeTo(ConfigurationSection section) {
        section.set("name", playerName);
        section.set("template", templateKey);
        section.set("reason", reason);
        section.set("actor", actor);
        section.set("ban-id", banId);
        section.set("numeric-id", numericId);
        section.set("created-at", createdAtMillis);
        section.set("expires-at", expiresAtMillis);
    }

    public void writeToHistory(ConfigurationSection section) {
        section.set("uuid", uniqueId.toString());
        writeTo(section);
    }

    public static BanEntry from(ConfigurationSection section) {
        UUID uniqueId = UUID.fromString(section.getName());
        String playerName = section.getString("name", "unknown");
        String templateKey = section.getString("template", "cheating");
        String reason = section.getString("reason", "No reason provided");
        String actor = section.getString("actor", "CONSOLE");
        String banId = section.getString("ban-id", "UNKNOWN");
        String numericId = section.getString("numeric-id", "000000");
        long createdAt = section.getLong("created-at", System.currentTimeMillis());
        long expiresAt = section.getLong("expires-at", -1L);
        return new BanEntry(uniqueId, playerName, templateKey, reason, actor, banId, numericId, createdAt, expiresAt);
    }

    public static BanEntry fromHistory(ConfigurationSection section) {
        UUID uniqueId = UUID.fromString(section.getString("uuid"));
        String playerName = section.getString("name", "unknown");
        String templateKey = section.getString("template", "cheating");
        String reason = section.getString("reason", "No reason provided");
        String actor = section.getString("actor", "CONSOLE");
        String banId = section.getString("ban-id", "UNKNOWN");
        String numericId = section.getString("numeric-id", "000000");
        long createdAt = section.getLong("created-at", System.currentTimeMillis());
        long expiresAt = section.getLong("expires-at", -1L);
        return new BanEntry(uniqueId, playerName, templateKey, reason, actor, banId, numericId, createdAt, expiresAt);
    }
}

