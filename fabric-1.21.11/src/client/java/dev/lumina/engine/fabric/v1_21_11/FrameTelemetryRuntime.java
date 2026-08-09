package dev.lumina.engine.fabric.v1_21_11;

import dev.lumina.engine.common.QualityProfile;
import dev.lumina.engine.common.adaptive.AdaptiveRecommendationEngine;
import dev.lumina.engine.common.adaptive.ActionableRecommendation;
import dev.lumina.engine.common.adaptive.ActionableRecommendationTracker;
import dev.lumina.engine.common.adaptive.RecommendationReason;
import dev.lumina.engine.common.adaptive.RecommendationResult;
import dev.lumina.engine.common.adaptive.QualityAdjustmentPlan;
import dev.lumina.engine.common.adaptive.QualityAdjustmentPlanner;
import java.lang.ref.WeakReference;
import java.util.Optional;
import dev.lumina.engine.common.telemetry.FrameTimeMonitor;
import dev.lumina.engine.common.telemetry.FrameTimeSnapshot;
import dev.lumina.engine.common.benchmark.BenchmarkSnapshot;
import dev.lumina.engine.common.benchmark.GuidedBenchmarkSession;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;

final class FrameTelemetryRuntime {
    private static final long UPDATE_INTERVAL_NANOS = 1_000_000_000L;
    private static final FrameTimeMonitor MONITOR = new FrameTimeMonitor();
    private static final AdaptiveRecommendationEngine RECOMMENDATIONS = new AdaptiveRecommendationEngine();
    private static final ActionableRecommendationTracker ACTIONABLE = new ActionableRecommendationTracker();
    private static final GuidedBenchmarkSession BENCHMARK = new GuidedBenchmarkSession();
    private static int targetFps = 60;
    private static QualityProfile profile = QualityProfile.BALANCED;
    private static WeakReference<Object> lastWorld = new WeakReference<>(null);
    private static String lastIrisState;
    private static long nextUpdate;
    private static FrameTimeSnapshot latest = FrameTimeSnapshot.warmingUp(0, targetFps);
    private static RecommendationResult recommendation = RecommendationResult.hold(RecommendationReason.WARMING_UP);
    private static QualityAdjustmentPlan plan = QualityAdjustmentPlanner.plan(profile, recommendation.recommendation());

    private FrameTelemetryRuntime() {}

    static void initialize(int configuredTargetFps, QualityProfile configuredProfile) {
        targetFps = configuredTargetFps;
        profile = configuredProfile;
        latest = FrameTimeSnapshot.warmingUp(0, targetFps);
        WorldRenderEvents.END_MAIN.register(context -> {
            Object world = MinecraftClient.getInstance().world;
            if (world != lastWorld.get()) {
                lastWorld = new WeakReference<>(world);
                resetContext(System.nanoTime());
                return;
            }
            record(System.nanoTime());
        });
    }

    static void setTargetFps(int value) {
        targetFps = value;
        RECOMMENDATIONS.onTargetChanged();
        latest = MONITOR.snapshot(value);
        recommendation = RecommendationResult.hold(RecommendationReason.WARMING_UP);
        plan = QualityAdjustmentPlanner.plan(profile, recommendation.recommendation());
    }
    static void setProfile(QualityProfile value) {
        profile = value;
        RECOMMENDATIONS.resetObservations();
        recommendation = RecommendationResult.hold(RecommendationReason.WARMING_UP);
        plan = QualityAdjustmentPlanner.plan(profile, recommendation.recommendation());
    }
    static FrameTimeSnapshot latest() { return latest; }
    static RecommendationResult recommendation() { return recommendation; }
    static QualityAdjustmentPlan plan() { return plan; }
    static Optional<ActionableRecommendation> actionableRecommendation() { return ACTIONABLE.current(); }
    static void dismissActionableRecommendation() { ACTIONABLE.dismiss(); }
    static void startBenchmark() { BENCHMARK.start(System.nanoTime()); }
    static void cancelBenchmark() { BENCHMARK.cancel(); }
    static BenchmarkSnapshot benchmark() { return BENCHMARK.snapshot(System.nanoTime()); }

    private static void record(long now) {
        if (now >= nextUpdate) {
            String irisState = irisState();
            if (lastIrisState != null && !lastIrisState.equals(irisState)) {
                lastIrisState = irisState;
                resetContext(now);
                return;
            }
            lastIrisState = irisState;
        }
        MONITOR.recordFrame(now);
        if (now >= nextUpdate) {
            latest = MONITOR.snapshot(targetFps);
            recommendation = RECOMMENDATIONS.evaluate(latest, profile, now);
            plan = QualityAdjustmentPlanner.plan(profile, recommendation.recommendation());
            ACTIONABLE.observe(recommendation, profile, now, System.currentTimeMillis());
            BENCHMARK.update(latest, now, System.currentTimeMillis());
            nextUpdate = now + UPDATE_INTERVAL_NANOS;
        }
    }

    private static void resetContext(long now) {
        MONITOR.reset();
        RECOMMENDATIONS.onContextChanged();
        latest = FrameTimeSnapshot.warmingUp(0, targetFps);
        recommendation = RecommendationResult.hold(RecommendationReason.WARMING_UP);
        plan = QualityAdjustmentPlanner.plan(profile, recommendation.recommendation());
        ACTIONABLE.resetContext();
        BENCHMARK.cancel();
        nextUpdate = now + UPDATE_INTERVAL_NANOS;
    }

    private static String irisState() {
        var status = new IrisIntegration(new FabricPlatformAdapter()).status();
        return status.installed() + ":" + status.shadersEnabled() + ":" + status.shaderActive();
    }
}
