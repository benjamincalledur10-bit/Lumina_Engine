package dev.lumina.engine.common.adaptive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import dev.lumina.engine.common.QualityProfile;
import dev.lumina.engine.common.telemetry.FrameTimeSnapshot;
import dev.lumina.engine.common.telemetry.TargetStatus;
import org.junit.jupiter.api.Test;

class AdaptiveRecommendationEngineTest {
    private final AdaptiveRecommendationEngine engine = new AdaptiveRecommendationEngine();

    @Test void stablePerformanceHolds() { assertRecommendation(QualityRecommendation.HOLD, evaluate(60, QualityProfile.BALANCED, 0)); }
    @Test void briefDropHolds() { evaluate(50, QualityProfile.BALANCED, 0); assertRecommendation(QualityRecommendation.HOLD, evaluate(50, QualityProfile.BALANCED, 1)); }
    @Test void sustainedDropLowers() { evaluate(50, QualityProfile.BALANCED, 0); evaluate(50, QualityProfile.BALANCED, 1); assertRecommendation(QualityRecommendation.LOWER_QUALITY, evaluate(50, QualityProfile.BALANCED, 2)); }
    @Test void sustainedRecoveryRaises() { for (int i=0;i<9;i++) evaluate(70, QualityProfile.BALANCED, i); assertRecommendation(QualityRecommendation.RAISE_QUALITY, evaluate(70, QualityProfile.BALANCED, 9)); }
    @Test void hysteresisPreventsOscillation() { evaluate(50, QualityProfile.BALANCED, 0); evaluate(60, QualityProfile.BALANCED, 1); evaluate(50, QualityProfile.BALANCED, 2); assertRecommendation(QualityRecommendation.HOLD, evaluate(50, QualityProfile.BALANCED, 3)); }
    @Test void lowerCooldownApplies() { lowerAt(0); evaluate(50, QualityProfile.BALANCED, 1); evaluate(50, QualityProfile.BALANCED, 2); assertReason(RecommendationReason.COOLDOWN, evaluate(50, QualityProfile.BALANCED, 3)); assertRecommendation(QualityRecommendation.LOWER_QUALITY, evaluate(50, QualityProfile.BALANCED, AdaptiveRecommendationEngine.LOWER_COOLDOWN_NANOS)); }
    @Test void raiseCooldownApplies() { raiseAt(0); for(int i=0;i<10;i++) evaluate(70, QualityProfile.BALANCED, i+1); assertReason(RecommendationReason.COOLDOWN, evaluate(70, QualityProfile.BALANCED, 11)); assertRecommendation(QualityRecommendation.RAISE_QUALITY, evaluate(70, QualityProfile.BALANCED, AdaptiveRecommendationEngine.RAISE_COOLDOWN_NANOS)); }
    @Test void warmingAndInvalidResetStreaks() { evaluate(50, QualityProfile.BALANCED, 0); evaluate(50, QualityProfile.BALANCED, 1); engine.evaluate(FrameTimeSnapshot.warmingUp(0,60), QualityProfile.BALANCED,2); assertRecommendation(QualityRecommendation.HOLD, evaluate(50, QualityProfile.BALANCED,3)); }
    @Test void respectsQualityLimits() { evaluate(50, QualityProfile.PERFORMANCE,0); evaluate(50, QualityProfile.PERFORMANCE,1); assertReason(RecommendationReason.QUALITY_FLOOR,evaluate(50,QualityProfile.PERFORMANCE,2)); RecommendationResult result = null; for(int i=0;i<10;i++) result=evaluate(70,QualityProfile.CINEMATIC,20+i); assertReason(RecommendationReason.QUALITY_CEILING,result); }
    @Test void insufficientSamplesWarmUp() { assertReason(RecommendationReason.WARMING_UP, engine.evaluate(snapshot(50,10),QualityProfile.BALANCED,0)); }
    @Test void stableMinimumIsPrimaryEvenWhenAverageIsHigh() {
        FrameTimeSnapshot snapshot = new FrameTimeSnapshot(100, 120, 50, 45, 8.3, 20, 60, TargetStatus.MEETING_TARGET);
        engine.evaluate(snapshot, QualityProfile.BALANCED, 0);
        engine.evaluate(snapshot, QualityProfile.BALANCED, 1);
        assertRecommendation(QualityRecommendation.LOWER_QUALITY, engine.evaluate(snapshot, QualityProfile.BALANCED, 2));
    }
    @Test void nonFiniteMetricResetsAsInvalid() {
        FrameTimeSnapshot invalid = new FrameTimeSnapshot(100, 60, Double.NaN, 50, 16, 20, 60, TargetStatus.MEETING_TARGET);
        assertReason(RecommendationReason.WARMING_UP, engine.evaluate(invalid, QualityProfile.BALANCED, 0));
    }

    private void lowerAt(long now) { evaluate(50,QualityProfile.BALANCED,now); evaluate(50,QualityProfile.BALANCED,now); evaluate(50,QualityProfile.BALANCED,now); }
    private void raiseAt(long now) { for(int i=0;i<10;i++) evaluate(70,QualityProfile.BALANCED,now); }
    private RecommendationResult evaluate(double stable, QualityProfile profile, long now) { return engine.evaluate(snapshot(stable,100),profile,now); }
    private static FrameTimeSnapshot snapshot(double stable,int samples) { return new FrameTimeSnapshot(samples,stable,stable,stable,1000/stable,1000/stable,60,TargetStatus.MEETING_TARGET); }
    private static void assertRecommendation(QualityRecommendation value,RecommendationResult result){ assertEquals(value,result.recommendation()); }
    private static void assertReason(RecommendationReason value,RecommendationResult result){ assertEquals(value,result.reason()); }
}
