package dev.lumina.engine.common;

import java.util.Objects;

public final class LuminaConfig {
    public static final int MIN_TARGET_FPS = 30;
    public static final int MAX_TARGET_FPS = 240;
    public static final int DEFAULT_TARGET_FPS = 60;

    private QualityProfile profile;
    private int targetFps;
    private boolean adaptiveOptimizationEnabled;

    public LuminaConfig() {
        this(QualityProfile.BALANCED, DEFAULT_TARGET_FPS, false);
    }

    public LuminaConfig(QualityProfile profile, int targetFps, boolean adaptiveOptimizationEnabled) {
        this.profile = Objects.requireNonNull(profile, "profile");
        setTargetFps(targetFps);
        this.adaptiveOptimizationEnabled = adaptiveOptimizationEnabled;
    }

    public static LuminaConfig defaults() {
        return new LuminaConfig();
    }

    public LuminaConfig copy() {
        return new LuminaConfig(profile, targetFps, adaptiveOptimizationEnabled);
    }

    public QualityProfile profile() {
        return profile;
    }

    public void setProfile(QualityProfile profile) {
        this.profile = Objects.requireNonNull(profile, "profile");
    }

    public int targetFps() {
        return targetFps;
    }

    public void setTargetFps(int targetFps) {
        if (targetFps < MIN_TARGET_FPS || targetFps > MAX_TARGET_FPS) {
            throw new IllegalArgumentException(
                "Target FPS must be between " + MIN_TARGET_FPS + " and " + MAX_TARGET_FPS
            );
        }
        this.targetFps = targetFps;
    }

    public boolean adaptiveOptimizationEnabled() {
        return adaptiveOptimizationEnabled;
    }

    public void setAdaptiveOptimizationEnabled(boolean adaptiveOptimizationEnabled) {
        this.adaptiveOptimizationEnabled = adaptiveOptimizationEnabled;
    }
}
