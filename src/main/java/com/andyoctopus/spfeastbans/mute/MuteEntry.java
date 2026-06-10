package com.andyoctopus.spfeastbans.mute;

import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

public final class MuteEntry {
    private final UUID uniqueId;
    private final String playerName;
    private final String reasonKey;
    private final String actor;
    private final String muteId;
    private final long createdAtMillis;
    private final long expiresAtMillis;
    private boolean notificationSent;

    public MuteEntry(UUID uniqueId,
                     String playerName,
                     String reasonKey,
                     String actor,
                     String muteId,
                     long createdAtMillis,
                     long expiresAtMillis) {
        this(uniqueId, playerName, reasonKey, actor, muteId, createdAtMillis, expiresAtMillis, false);
    }

    public MuteEntry(UUID uniqueId,
                     String playerName,
                     String reasonKey,
                     String actor,
                     String muteId,
                     long createdAtMillis,
                     long expiresAtMillis,
                     boolean notificationSent) {
        this.uniqueId = uniqueId;
        this.playerName = playerName;
        this.reasonKey = reasonKey;
        this.actor = actor;
        this.muteId = muteId;
        this.createdAtMillis = createdAtMillis;
        this.expiresAtMillis = expiresAtMillis;
        this.notificationSent = notificationSent;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getReasonKey() {
        return reasonKey;
    }

    public String getActor() {
        return actor;
    }

    public String getMuteId() {
        return muteId;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }

    public long getExpiresAtMillis() {
        return expiresAtMillis;
    }

    public boolean isNotificationSent() {
        return notificationSent;
    }

    public void markNotificationSent() {
        this.notificationSent = true;
    }

    public boolean isExpired(long now) {
        return expiresAtMillis <= now;
    }

    public void writeTo(ConfigurationSection section) {
        section.set("name", playerName);
        section.set("reason-key", reasonKey);
        section.set("actor", actor);
        section.set("mute-id", muteId);
        section.set("created-at", createdAtMillis);
        section.set("expires-at", expiresAtMillis);
        section.set("notification-sent", notificationSent);
    }

    public void writeToHistory(ConfigurationSection section) {
        section.set("uuid", uniqueId.toString());
        writeTo(section);
    }

    public static MuteEntry from(ConfigurationSection section) {
        UUID uniqueId = UUID.fromString(section.getName());
        String playerName = section.getString("name", "unknown");
        String reasonKey = section.getString("reason-key", "minorchat");
        String actor = section.getString("actor", "CONSOLE");
        String muteId = section.getString("mute-id", "UNKNOWN");
        long createdAt = section.getLong("created-at", System.currentTimeMillis());
        long expiresAt = section.getLong("expires-at", System.currentTimeMillis());
        boolean notificationSent = section.getBoolean("notification-sent", false);
        return new MuteEntry(uniqueId, playerName, reasonKey, actor, muteId, createdAt, expiresAt, notificationSent);
    }

    public static MuteEntry fromHistory(ConfigurationSection section) {
        UUID uniqueId = UUID.fromString(section.getString("uuid"));
        String playerName = section.getString("name", "unknown");
        String reasonKey = section.getString("reason-key", "minorchat");
        String actor = section.getString("actor", "CONSOLE");
        String muteId = section.getString("mute-id", "UNKNOWN");
        long createdAt = section.getLong("created-at", System.currentTimeMillis());
        long expiresAt = section.getLong("expires-at", System.currentTimeMillis());
        boolean notificationSent = section.getBoolean("notification-sent", false);
        return new MuteEntry(uniqueId, playerName, reasonKey, actor, muteId, createdAt, expiresAt, notificationSent);
    }
}
