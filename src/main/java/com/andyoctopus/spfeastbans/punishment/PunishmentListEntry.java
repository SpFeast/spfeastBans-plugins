package com.andyoctopus.spfeastbans.punishment;

import java.util.UUID;

public record PunishmentListEntry(
        Type type,
        UUID uniqueId,
        String playerName,
        String categoryKey,
        String actor,
        String referenceId,
        long createdAtMillis,
        long expiresAtMillis
) {
    public enum Type {
        BAN,
        MUTE
    }

    public boolean isPermanent() {
        return expiresAtMillis < 0L;
    }

    public String referenceLabel() {
        return type == Type.BAN ? "Ban ID" : "Mute ID";
    }

    public String listTag() {
        return type == Type.BAN ? "BAN:" + categoryKey : "MUTE:" + categoryKey;
    }
}
