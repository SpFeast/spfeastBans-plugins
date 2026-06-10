package com.andyoctopus.spfeastbans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BanTemplate {
    private final String name;
    private final List<String> lines;
    private final boolean usesDurationPlaceholder;
    private final boolean usesReasonPlaceholder;

    BanTemplate(String name, List<String> lines) {
        this.name = name;
        this.lines = Collections.unmodifiableList(new ArrayList<>(lines));
        this.usesDurationPlaceholder = containsPlaceholder("%DURATION%");
        this.usesReasonPlaceholder = containsPlaceholder("%REASON%");
    }

    public String getName() {
        return name;
    }

    public boolean usesDurationPlaceholder() {
        return usesDurationPlaceholder;
    }

    public boolean usesReasonPlaceholder() {
        return usesReasonPlaceholder;
    }

    public List<String> render(String duration,
                               String reason,
                               String banId,
                               String numericId,
                               String date) {
        List<String> rendered = new ArrayList<>(lines.size());
        for (String line : lines) {
            String result = line;
            result = replaceToken(result, "%DURATION%", safe(duration));
            result = replaceToken(result, "%REASON%", safe(reason));
            result = replaceToken(result, "%banid%", banId);
            result = replaceToken(result, "%BANID%", banId);
            result = replaceToken(result, "%ID%", numericId);
            result = replaceToken(result, "%DATE%", date);
            rendered.add(applyColorCodes(result));
        }
        return rendered;
    }

    private String replaceToken(String input, String token, String value) {
        if (value == null) {
            value = "";
        }
        return input.replace(token, value);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean containsPlaceholder(String token) {
        for (String line : lines) {
            if (line.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private String applyColorCodes(String value) {
        return value.replace('&', '\u00A7');
    }
}

