package dev.lumina.engine.common.adaptive;

import dev.lumina.engine.common.QualityProfile;
import java.util.List;

public final class QualityAdjustmentPlanner {
    private static final List<QualityProfile> ORDERED_PROFILES = List.of(
        QualityProfile.PERFORMANCE,
        QualityProfile.BALANCED,
        QualityProfile.QUALITY,
        QualityProfile.CINEMATIC
    );

    private QualityAdjustmentPlanner() {}

    public static QualityAdjustmentPlan plan(QualityProfile current, QualityRecommendation recommendation) {
        int index = ORDERED_PROFILES.indexOf(current);
        if (index < 0 || recommendation == QualityRecommendation.HOLD) return hold(current);

        int suggestedIndex = switch (recommendation) {
            case LOWER_QUALITY -> Math.max(0, index - 1);
            case RAISE_QUALITY -> Math.min(ORDERED_PROFILES.size() - 1, index + 1);
            case HOLD -> index;
        };
        QualityProfile suggested = ORDERED_PROFILES.get(suggestedIndex);
        if (suggested == current) return hold(current);

        AdjustmentDirection direction = suggestedIndex > index
            ? AdjustmentDirection.INCREASE
            : AdjustmentDirection.DECREASE;
        List<PlannedAdjustment> adjustments = List.of(
            new PlannedAdjustment(AdjustmentDomain.SHADER_QUALITY, direction),
            new PlannedAdjustment(AdjustmentDomain.SHADOW_QUALITY, direction),
            new PlannedAdjustment(AdjustmentDomain.LIGHTING_EFFECTS, direction),
            new PlannedAdjustment(AdjustmentDomain.DISTANT_HORIZONS_QUALITY, direction)
        );
        return new QualityAdjustmentPlan(current, suggested, recommendation, adjustments);
    }

    private static QualityAdjustmentPlan hold(QualityProfile current) {
        return new QualityAdjustmentPlan(current, current, QualityRecommendation.HOLD, List.of());
    }
}
