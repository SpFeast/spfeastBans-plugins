package com.andyoctopus.spfeastapi;

import java.util.List;

public interface SpfeastApi {
    PunishmentResult tempBan(String targetQuery, String templateKey, String durationText, String reason, String actorName);

    PunishmentResult tempMute(String targetQuery, String reasonKey, String durationText, String actorName);

    List<String> getBanTemplateKeys();

    List<String> getMuteReasonKeys();
}
