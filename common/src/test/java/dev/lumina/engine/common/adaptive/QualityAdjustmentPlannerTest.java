package dev.lumina.engine.common.adaptive;

import static org.junit.jupiter.api.Assertions.*;
import dev.lumina.engine.common.QualityProfile;
import org.junit.jupiter.api.Test;

class QualityAdjustmentPlannerTest {
    @Test void lowersExactlyOneLevel() {
        QualityAdjustmentPlan plan = QualityAdjustmentPlanner.plan(QualityProfile.BALANCED, QualityRecommendation.LOWER_QUALITY);
        assertEquals(QualityProfile.PERFORMANCE, plan.suggestedProfile());
        assertTrue(plan.adjustments().stream().allMatch(change -> change.direction() == AdjustmentDirection.DECREASE));
    }

    @Test void raisesExactlyOneLevel() {
        QualityAdjustmentPlan plan = QualityAdjustmentPlanner.plan(QualityProfile.BALANCED, QualityRecommendation.RAISE_QUALITY);
        assertEquals(QualityProfile.QUALITY, plan.suggestedProfile());
        assertTrue(plan.adjustments().stream().allMatch(change -> change.direction() == AdjustmentDirection.INCREASE));
    }

    @Test void holdsAtLowerAndUpperBounds() {
        assertFalse(QualityAdjustmentPlanner.plan(QualityProfile.PERFORMANCE, QualityRecommendation.LOWER_QUALITY).changesQuality());
        assertFalse(QualityAdjustmentPlanner.plan(QualityProfile.CINEMATIC, QualityRecommendation.RAISE_QUALITY).changesQuality());
    }

    @Test void holdAndCustomHaveNoPlannedChanges() {
        assertTrue(QualityAdjustmentPlanner.plan(QualityProfile.QUALITY, QualityRecommendation.HOLD).adjustments().isEmpty());
        assertFalse(QualityAdjustmentPlanner.plan(QualityProfile.CUSTOM, QualityRecommendation.RAISE_QUALITY).changesQuality());
    }

    @Test void exposesOnlyImmutablePreviewData() {
        QualityAdjustmentPlan plan = QualityAdjustmentPlanner.plan(QualityProfile.QUALITY, QualityRecommendation.LOWER_QUALITY);
        assertThrows(UnsupportedOperationException.class, () -> plan.adjustments().clear());
    }
}
