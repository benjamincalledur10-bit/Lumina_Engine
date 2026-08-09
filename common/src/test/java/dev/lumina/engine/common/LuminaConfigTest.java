package dev.lumina.engine.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class LuminaConfigTest {
    @org.junit.jupiter.api.Test
    void hudDefaultsOffAtTopLeft() {
        LuminaConfig config = LuminaConfig.defaults();
        org.junit.jupiter.api.Assertions.assertFalse(config.performanceHudEnabled());
        org.junit.jupiter.api.Assertions.assertEquals(HudPosition.TOP_LEFT, config.performanceHudPosition());
    }
    @Test
    void defaultsAreBalancedAtSixtyFpsWithAdaptiveOptimizationDisabled() {
        LuminaConfig config = LuminaConfig.defaults();

        assertEquals(QualityProfile.BALANCED, config.profile());
        assertEquals(60, config.targetFps());
        assertFalse(config.adaptiveOptimizationEnabled());
    }

    @Test
    void acceptsInclusiveFpsBoundaries() {
        assertEquals(30, new LuminaConfig(QualityProfile.BALANCED, 30, false).targetFps());
        assertEquals(240, new LuminaConfig(QualityProfile.BALANCED, 240, false).targetFps());
    }

    @Test
    void rejectsFpsOutsideValidRange() {
        assertThrows(IllegalArgumentException.class, () -> new LuminaConfig(QualityProfile.BALANCED, 29, false));
        assertThrows(IllegalArgumentException.class, () -> new LuminaConfig(QualityProfile.BALANCED, 241, false));
    }
}
