package dev.lumina.engine.common.adaptive;

public record RecommendationResult(QualityRecommendation recommendation, RecommendationReason reason) {
    public static RecommendationResult hold(RecommendationReason reason) {
        return new RecommendationResult(QualityRecommendation.HOLD, reason);
    }
}
