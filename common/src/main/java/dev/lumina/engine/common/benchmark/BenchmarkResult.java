package dev.lumina.engine.common.benchmark;

public record BenchmarkResult(
    long completedAtEpochMillis,
    int observations,
    double averageFps,
    double stableMinimumFps,
    double onePercentLowFps,
    double averageFrameTimeMillis,
    double p95FrameTimeMillis,
    double targetMetPercent,
    int targetFps
) {}
