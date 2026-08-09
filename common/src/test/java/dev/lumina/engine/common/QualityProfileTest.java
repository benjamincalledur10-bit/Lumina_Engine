package dev.lumina.engine.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

class QualityProfileTest {
    @ParameterizedTest
    @CsvSource({
        "Performance, PERFORMANCE",
        "balanced, BALANCED",
        "QUALITY, QUALITY",
        "Cinematic, CINEMATIC",
        "custom, CUSTOM"
    })
    void convertsProfileNames(String input, QualityProfile expected) {
        assertEquals(expected, QualityProfile.fromString(input));
        assertEquals(input.toLowerCase(), expected.serializedName().toLowerCase());
    }

    @Test
    void rejectsUnknownProfiles() {
        assertThrows(IllegalArgumentException.class, () -> QualityProfile.fromString("ultra"));
    }
}
