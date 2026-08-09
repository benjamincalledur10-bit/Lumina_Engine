package dev.lumina.engine.common.telemetry;

public record FrameTimeSnapshot(
    int sampleCount,
    double averageFps,
    double stableMinimumFps,
    double onePercentLowFps,
    double averageFrameTimeMillis,
    double p95FrameTimeMillis,
    int targetFps,
    TargetStatus targetStatus
) {
    public static FrameTimeSnapshot warmingUp(int sampleCount, int targetFps) {
        return new FrameTimeSnapshot(sampleCount, 0, 0, 0, 0, 0, targetFps, TargetStatus.WARMING_UP);
    }
}
