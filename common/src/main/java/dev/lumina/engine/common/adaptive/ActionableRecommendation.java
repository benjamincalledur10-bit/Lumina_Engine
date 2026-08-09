package dev.lumina.engine.common.adaptive;

import dev.lumina.engine.common.QualityProfile;

public record ActionableRecommendation(
    RecommendationResult result,
    QualityAdjustmentPlan plan,
    long generatedAtNanos,
    long generatedAtEpochMillis,
    long cooldownEndsAtNanos
) {
    public long cooldownRemainingNanos(long nowNanos) {
        return Math.max(0L, cooldownEndsAtNanos - nowNanos);
    }

    public QualityProfile suggestedProfile() {
        return plan.suggestedProfile();
    }
}
