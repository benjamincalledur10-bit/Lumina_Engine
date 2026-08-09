package dev.lumina.engine.common.shader;

import dev.lumina.engine.common.QualityProfile;
import java.util.Optional;

public final class LuminaShaderPresetGuide {
    private LuminaShaderPresetGuide() {}

    public static Optional<Guidance> forProfile(QualityProfile profile) {
        if (profile == null || profile == QualityProfile.CUSTOM) return Optional.empty();
        String preset = switch (profile) {
            case PERFORMANCE -> "Performance";
            case BALANCED -> "Balanced";
            case QUALITY -> "Quality";
            case CINEMATIC -> "Cinematic";
            case CUSTOM -> throw new IllegalStateException();
        };
        return Optional.of(new Guidance(preset, preset));
    }

    public record Guidance(String eventHorizonPreset, String luminaLitePreset) {}
}
