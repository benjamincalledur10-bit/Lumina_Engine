package dev.lumina.engine.common.adaptive;

import dev.lumina.engine.common.QualityProfile;
import java.util.Optional;

public final class ActionableRecommendationTracker {
    private ActionableRecommendation current;

    public void observe(RecommendationResult result, QualityProfile profile, long nowNanos, long nowEpochMillis) {
        if (result == null || result.recommendation() == QualityRecommendation.HOLD) return;
        QualityAdjustmentPlan plan = QualityAdjustmentPlanner.plan(profile, result.recommendation());
        if (!plan.changesQuality()) return;
        long cooldown = result.recommendation() == QualityRecommendation.LOWER_QUALITY
            ? AdaptiveRecommendationEngine.LOWER_COOLDOWN_NANOS
            : AdaptiveRecommendationEngine.RAISE_COOLDOWN_NANOS;
        current = new ActionableRecommendation(result, plan, nowNanos, nowEpochMillis, nowNanos + cooldown);
    }

    public Optional<ActionableRecommendation> current() {
        return Optional.ofNullable(current);
    }

    public void dismiss() {
        current = null;
    }

    public void resetContext() {
        current = null;
    }
}
