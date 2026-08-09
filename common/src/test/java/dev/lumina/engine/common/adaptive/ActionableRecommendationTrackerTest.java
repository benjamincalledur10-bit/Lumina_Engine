package dev.lumina.engine.common.adaptive;

import static org.junit.jupiter.api.Assertions.*;

import dev.lumina.engine.common.QualityProfile;
import org.junit.jupiter.api.Test;

class ActionableRecommendationTrackerTest {
    @Test
    void retainsImportantRecommendationAcrossHoldUpdates() {
        ActionableRecommendationTracker tracker = new ActionableRecommendationTracker();
        tracker.observe(new RecommendationResult(QualityRecommendation.LOWER_QUALITY,
            RecommendationReason.SUSTAINED_LOW_PERFORMANCE), QualityProfile.BALANCED, 100L, 1_000L);
        tracker.observe(RecommendationResult.hold(RecommendationReason.COOLDOWN), QualityProfile.BALANCED, 200L, 1_100L);
        assertEquals(QualityProfile.PERFORMANCE, tracker.current().orElseThrow().suggestedProfile());
    }

    @Test
    void reportsCooldownAndSupportsDismissal() {
        ActionableRecommendationTracker tracker = new ActionableRecommendationTracker();
        tracker.observe(new RecommendationResult(QualityRecommendation.RAISE_QUALITY,
            RecommendationReason.SUSTAINED_HEADROOM), QualityProfile.BALANCED, 10L, 1_000L);
        ActionableRecommendation value = tracker.current().orElseThrow();
        assertEquals(AdaptiveRecommendationEngine.RAISE_COOLDOWN_NANOS - 5L,
            value.cooldownRemainingNanos(15L));
        tracker.dismiss();
        assertTrue(tracker.current().isEmpty());
    }

    @Test
    void contextResetClearsStaleAction() {
        ActionableRecommendationTracker tracker = new ActionableRecommendationTracker();
        tracker.observe(new RecommendationResult(QualityRecommendation.LOWER_QUALITY,
            RecommendationReason.SUSTAINED_LOW_PERFORMANCE), QualityProfile.QUALITY, 0L, 1_000L);
        tracker.resetContext();
        assertTrue(tracker.current().isEmpty());
    }
}
