package dev.lumina.engine.common.shader;

import static org.junit.jupiter.api.Assertions.*;
import dev.lumina.engine.common.QualityProfile;
import org.junit.jupiter.api.Test;

class LuminaShaderPresetGuideTest {
    @Test void mapsEveryCoordinatedProfileToSameNamedShaderPreset() {
        for (QualityProfile profile : new QualityProfile[] {QualityProfile.PERFORMANCE,
            QualityProfile.BALANCED, QualityProfile.QUALITY, QualityProfile.CINEMATIC}) {
            var guide = LuminaShaderPresetGuide.forProfile(profile).orElseThrow();
            assertEquals(profile.serializedName(), guide.eventHorizonPreset());
            assertEquals(guide.eventHorizonPreset(), guide.luminaLitePreset());
        }
    }

    @Test void customRequiresManualChoice() {
        assertTrue(LuminaShaderPresetGuide.forProfile(QualityProfile.CUSTOM).isEmpty());
    }
}
