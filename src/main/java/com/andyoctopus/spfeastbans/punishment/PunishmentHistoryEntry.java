package com.andyoctopus.spfeastbans.punishment;

import java.util.UUID;

public record PunishmentHistoryEntry(
        PunishmentListEntry.Type type,
        UUID uniqueId,
        String playerName,
        String categoryKey,
        String actor,
        String referenceId,
        long createdAtMillis,
        String createdAtText,
        String expiresAtText,
        String detailText
) {
    public String referenceLabel() {
        return type == PunishmentListEntry.Type.BAN ? "Ban ID" : "Mute ID";
    }

    public String listTag() {
        return type == PunishmentListEntry.Type.BAN ? "BAN:" + categoryKey : "MUTE:" + categoryKey;
    }
}
