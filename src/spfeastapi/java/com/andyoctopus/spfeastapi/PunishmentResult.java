package com.andyoctopus.spfeastapi;

import java.util.UUID;

public record PunishmentResult(boolean success,
                               String message,
                               UUID targetUuid,
                               String targetName,
                               String referenceId) {
    public static PunishmentResult success(UUID targetUuid, String targetName, String referenceId) {
        return new PunishmentResult(true, "OK", targetUuid, targetName, referenceId);
    }

    public static PunishmentResult failure(String message) {
        return new PunishmentResult(false, message, null, null, null);
    }
}
