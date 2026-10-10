package com.kairos.api.mongo.config;

/** Spring properties imports preserve shell-style surrounding quotes. */
public final class ExternalEnvValue {
    private ExternalEnvValue() { }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.strip();
        if (normalized.length() >= 2) {
            int first = normalized.charAt(0);
            int last = normalized.charAt(normalized.length() - 1);
            if ((first == 34 || first == 39) && first == last) {
                return normalized.substring(1, normalized.length() - 1);
            }
        }
        return normalized;
    }
}
