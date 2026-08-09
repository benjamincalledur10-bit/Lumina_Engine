package dev.lumina.engine.fabric.v26_2;

import dev.lumina.engine.common.telemetry.FrameTimeMonitor;
import dev.lumina.engine.common.telemetry.FrameTimeSnapshot;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

final class FrameTelemetryRuntime {
    private static final long UPDATE_INTERVAL_NANOS = 1_000_000_000L;
    private static final FrameTimeMonitor MONITOR = new FrameTimeMonitor();
    private static int targetFps = 60;
    private static long nextUpdate;
    private static FrameTimeSnapshot latest = FrameTimeSnapshot.warmingUp(0, targetFps);

    private FrameTelemetryRuntime() {}

    static void initialize(int configuredTargetFps) {
        targetFps = configuredTargetFps;
        latest = FrameTimeSnapshot.warmingUp(0, targetFps);
        LevelRenderEvents.END_MAIN.register(context -> record(System.nanoTime()));
    }

    static void setTargetFps(int value) { targetFps = value; latest = MONITOR.snapshot(value); }
    static FrameTimeSnapshot latest() { return latest; }

    private static void record(long now) {
        MONITOR.recordFrame(now);
        if (now >= nextUpdate) {
            latest = MONITOR.snapshot(targetFps);
            nextUpdate = now + UPDATE_INTERVAL_NANOS;
        }
    }
}
