package dev.lumina.engine.common.adaptive;

import dev.lumina.engine.common.QualityProfile;
import dev.lumina.engine.common.telemetry.FrameTimeSnapshot;
import dev.lumina.engine.common.telemetry.TargetStatus;

public final class AdaptiveRecommendationEngine {
    public static final int MINIMUM_SAMPLES = 60;
    public static final long LOWER_COOLDOWN_NANOS = 15_000_000_000L;
    public static final long RAISE_COOLDOWN_NANOS = 30_000_000_000L;
    private static final double LOWER_THRESHOLD = 0.90;
    private static final double RAISE_THRESHOLD = 1.10;
    private int lowCount;
    private int highCount;
    private long cooldownUntil;

    public RecommendationResult evaluate(FrameTimeSnapshot snapshot, QualityProfile profile, long nowNanos) {
        if (!valid(snapshot)) {
            resetObservations();
            return RecommendationResult.hold(RecommendationReason.WARMING_UP);
        }
        if (profile == QualityProfile.CUSTOM) {
            resetCounters();
            return RecommendationResult.hold(RecommendationReason.CUSTOM_PROFILE);
        }

        double stableFps = snapshot.stableMinimumFps();
        if (stableFps < snapshot.targetFps() * LOWER_THRESHOLD) {
            lowCount++;
            highCount = 0;
            if (lowCount < 3) return RecommendationResult.hold(RecommendationReason.OBSERVING_LOW_PERFORMANCE);
            if (nowNanos < cooldownUntil) return RecommendationResult.hold(RecommendationReason.COOLDOWN);
            resetCounters();
            if (profile == QualityProfile.PERFORMANCE) return RecommendationResult.hold(RecommendationReason.QUALITY_FLOOR);
            cooldownUntil = nowNanos + LOWER_COOLDOWN_NANOS;
            return new RecommendationResult(QualityRecommendation.LOWER_QUALITY, RecommendationReason.SUSTAINED_LOW_PERFORMANCE);
        }
        if (stableFps > snapshot.targetFps() * RAISE_THRESHOLD) {
            highCount++;
            lowCount = 0;
            if (highCount < 10) return RecommendationResult.hold(RecommendationReason.OBSERVING_HEADROOM);
            if (nowNanos < cooldownUntil) return RecommendationResult.hold(RecommendationReason.COOLDOWN);
            resetCounters();
            if (profile == QualityProfile.CINEMATIC) return RecommendationResult.hold(RecommendationReason.QUALITY_CEILING);
            cooldownUntil = nowNanos + RAISE_COOLDOWN_NANOS;
            return new RecommendationResult(QualityRecommendation.RAISE_QUALITY, RecommendationReason.SUSTAINED_HEADROOM);
        }
        resetCounters();
        return RecommendationResult.hold(RecommendationReason.PERFORMANCE_STABLE);
    }

    public void reset() {
        resetObservations();
        cooldownUntil = 0;
    }

    public void resetObservations() { resetCounters(); }

    private static boolean valid(FrameTimeSnapshot snapshot) {
        return snapshot != null
            && snapshot.targetStatus() != TargetStatus.WARMING_UP
            && snapshot.sampleCount() >= MINIMUM_SAMPLES
            && Double.isFinite(snapshot.stableMinimumFps())
            && snapshot.stableMinimumFps() > 0;
    }

    private void resetCounters() { lowCount = 0; highCount = 0; }
}
