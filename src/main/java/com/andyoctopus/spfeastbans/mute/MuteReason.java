package com.andyoctopus.spfeastbans.mute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public enum MuteReason {
    UNDER_REVIEW(
            "underreview",
            "Under Review",
            86_400_000L,
            "A report against you is currently under review.",
            List.of(
                    "&c&m-----------------------------------------------------",
                    "&cA report against you is currently under review.",
                    "&7Your mute will expire in &f%DURATION%",
                    "&7",
                    "&7Find out more here: &e&n%MUTES_URL%",
                    "&7Mute ID: &f#%MUTEID%",
                    "&c&m-----------------------------------------------------"
            ),
            "areport", "a_report", "a-report", "report", "reportreview", "review", "ur", "ar"
    ),
    MINOR_CHAT(
            "minorchat",
            "Minor Chat",
            86_400_000L,
            "You are currently muted for a Minor Chat Infraction.",
            List.of(
                    "&c&m-----------------------------------------------------",
                    "&cYou are currently muted for a Minor Chat Infraction.",
                    "&7Your mute will expire in &f%DURATION%",
                    "&7",
                    "&7Find out more here: &e&n%MUTES_URL%",
                    "&7Mute ID: &f#%MUTEID%",
                    "&c&m-----------------------------------------------------"
            ),
            "minor_chat", "minor-chat", "minor", "mc"
    ),
    MAJOR_CHAT(
            "majorchat",
            "Major Chat",
            604_800_000L,
            "You are currently muted for a Major Chat Infraction.",
            List.of(
                    "&c&m-----------------------------------------------------",
                    "&cYou are currently muted for a Major Chat Infraction.",
                    "&7Your mute will expire in &f%DURATION%",
                    "&7",
                    "&7Find out more here: &e&n%MUTES_URL%",
                    "&7Mute ID: &f#%MUTEID%",
                    "&c&m-----------------------------------------------------"
            ),
            "major_chat", "major-chat", "major", "maj", "mj"
    );

    private static final Map<String, MuteReason> LOOKUP = new HashMap<>();

    static {
        for (MuteReason reason : values()) {
            LOOKUP.put(reason.key, reason);
            for (String alias : reason.aliases) {
                LOOKUP.put(alias.toLowerCase(Locale.ROOT), reason);
            }
        }
    }

    private final String key;
    private final String displayName;
    private final long defaultDurationMillis;
    private final String staffSummary;
    private final List<String> messageLines;
    private final List<String> aliases;

    MuteReason(String key,
               String displayName,
               long defaultDurationMillis,
               String staffSummary,
               List<String> messageLines,
               String... aliases) {
        this.key = key;
        this.displayName = displayName;
        this.defaultDurationMillis = defaultDurationMillis;
        this.staffSummary = staffSummary;
        this.messageLines = Collections.unmodifiableList(new ArrayList<>(messageLines));
        this.aliases = Collections.unmodifiableList(Arrays.asList(aliases));
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getDefaultDurationMillis() {
        return defaultDurationMillis;
    }

    public String getStaffSummary() {
        return staffSummary;
    }

    public List<String> render(String durationText, String muteId) {
        List<String> lines = new ArrayList<>(messageLines.size());
        for (String messageLine : messageLines) {
            String line = messageLine.replace("%DURATION%", durationText == null ? "" : durationText);
            line = line.replace("%MUTEID%", muteId == null ? "" : muteId);
            lines.add(line.replace('&', '\u00A7'));
        }
        return lines;
    }

    public static MuteReason get(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        return LOOKUP.get(input.toLowerCase(Locale.ROOT));
    }

    public static List<String> getKeys() {
        List<String> keys = new ArrayList<>();
        for (MuteReason reason : values()) {
            keys.add(reason.getKey());
        }
        return keys;
    }
}
