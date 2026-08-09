package dev.lumina.engine.common;

import java.util.Locale;

public enum QualityProfile {
    PERFORMANCE,
    BALANCED,
    QUALITY,
    CINEMATIC,
    CUSTOM;

    public static QualityProfile fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Profile must not be blank");
        }
        return valueOf(value.trim().replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT));
    }

    public String serializedName() {
        String lowerCaseName = name().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lowerCaseName.charAt(0)) + lowerCaseName.substring(1);
    }
}
