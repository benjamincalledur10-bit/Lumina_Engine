package dev.lumina.engine.fabric.v1_21_11;

import dev.lumina.engine.common.QualityProfile;
import dev.lumina.engine.common.adaptive.AdaptiveRecommendationEngine;
import dev.lumina.engine.common.adaptive.RecommendationReason;
import dev.lumina.engine.common.adaptive.RecommendationResult;
import dev.lumina.engine.common.telemetry.FrameTimeMonitor;
import dev.lumina.engine.common.telemetry.FrameTimeSnapshot;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;

final class FrameTelemetryRuntime {
    private static final long UPDATE_INTERVAL_NANOS = 1_000_000_000L;
    private static final FrameTimeMonitor MONITOR = new FrameTimeMonitor();
    private static final AdaptiveRecommendationEngine RECOMMENDATIONS = new AdaptiveRecommendationEngine();
    private static int targetFps = 60;
    private static QualityProfile profile = QualityProfile.BALANCED;
    private static Object lastWorld;
    private static String lastIrisState;
    private static long nextUpdate;
    private static FrameTimeSnapshot latest = FrameTimeSnapshot.warmingUp(0, targetFps);
    private static RecommendationResult recommendation = RecommendationResult.hold(RecommendationReason.WARMING_UP);

    private FrameTelemetryRuntime() {}

    static void initialize(int configuredTargetFps, QualityProfile configuredProfile) {
        targetFps = configuredTargetFps;
        profile = configuredProfile;
        latest = FrameTimeSnapshot.warmingUp(0, targetFps);
        WorldRenderEvents.END_MAIN.register(context -> {
            Object world = MinecraftClient.getInstance().world;
            if (world != lastWorld) {
                lastWorld = world;
                MONITOR.reset();
                RECOMMENDATIONS.resetObservations();
            }
            record(System.nanoTime());
        });
    }

    static void setTargetFps(int value) { targetFps = value; latest = MONITOR.snapshot(value); }
    static void setProfile(QualityProfile value) { profile = value; RECOMMENDATIONS.resetObservations(); }
    static FrameTimeSnapshot latest() { return latest; }
    static RecommendationResult recommendation() { return recommendation; }

    private static void record(long now) {
        MONITOR.recordFrame(now);
        if (now >= nextUpdate) {
            latest = MONITOR.snapshot(targetFps);
            String irisState = irisState();
            if (lastIrisState != null && !lastIrisState.equals(irisState)) RECOMMENDATIONS.resetObservations();
            lastIrisState = irisState;
            recommendation = RECOMMENDATIONS.evaluate(latest, profile, now);
            nextUpdate = now + UPDATE_INTERVAL_NANOS;
        }
    }

    private static String irisState() {
        var status = new IrisIntegration(new FabricPlatformAdapter()).status();
        return status.installed() + ":" + status.shadersEnabled() + ":" + status.shaderActive();
    }
}
