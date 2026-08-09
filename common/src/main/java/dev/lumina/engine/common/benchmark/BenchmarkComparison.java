package dev.lumina.engine.common.benchmark;

public record BenchmarkComparison(
    BenchmarkRecord baseline,
    BenchmarkRecord candidate,
    double averageFpsDelta,
    double averageFpsPercent,
    double onePercentLowDelta,
    double onePercentLowPercent,
    double p95FrameTimeDelta,
    double p95FrameTimePercent
) {
    public static BenchmarkComparison compare(BenchmarkRecord baseline, BenchmarkRecord candidate) {
        BenchmarkResult a = baseline.result();
        BenchmarkResult b = candidate.result();
        return new BenchmarkComparison(baseline, candidate,
            b.averageFps() - a.averageFps(), percent(a.averageFps(), b.averageFps()),
            b.onePercentLowFps() - a.onePercentLowFps(), percent(a.onePercentLowFps(), b.onePercentLowFps()),
            b.p95FrameTimeMillis() - a.p95FrameTimeMillis(), percent(a.p95FrameTimeMillis(), b.p95FrameTimeMillis()));
    }

    private static double percent(double before, double after) {
        return before == 0 ? 0 : (after - before) * 100.0 / before;
    }
}
