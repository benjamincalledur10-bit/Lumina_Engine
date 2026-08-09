package dev.lumina.engine.common.adaptive;

import dev.lumina.engine.common.QualityProfile;
import java.util.List;

public record QualityAdjustmentPlan(
    QualityProfile currentProfile,
    QualityProfile suggestedProfile,
    QualityRecommendation recommendation,
    List<PlannedAdjustment> adjustments
) {
    public QualityAdjustmentPlan {
        adjustments = List.copyOf(adjustments);
    }

    public boolean changesQuality() {
        return currentProfile != suggestedProfile;
    }
}
