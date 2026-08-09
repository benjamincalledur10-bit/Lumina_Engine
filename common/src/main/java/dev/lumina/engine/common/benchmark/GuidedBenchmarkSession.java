package dev.lumina.engine.common.benchmark;

import dev.lumina.engine.common.telemetry.FrameTimeSnapshot;
import dev.lumina.engine.common.telemetry.TargetStatus;
import java.util.Optional;

public final class GuidedBenchmarkSession {
    public static final long DEFAULT_WARMUP_NANOS = 10_000_000_000L;
    public static final long DEFAULT_MEASUREMENT_NANOS = 60_000_000_000L;
    private final long warmupNanos;
    private final long measurementNanos;
    private BenchmarkPhase phase = BenchmarkPhase.IDLE;
    private long phaseStarted;
    private int observations;
    private int targetMet;
    private double fpsTotal;
    private double stableMinimum = Double.POSITIVE_INFINITY;
    private double onePercentLow = Double.POSITIVE_INFINITY;
    private double frameTimeTotal;
    private double p95Maximum;
    private BenchmarkResult result;

    public GuidedBenchmarkSession() {
        this(DEFAULT_WARMUP_NANOS, DEFAULT_MEASUREMENT_NANOS);
    }

    public GuidedBenchmarkSession(long warmupNanos, long measurementNanos) {
        if (warmupNanos < 0 || measurementNanos <= 0) throw new IllegalArgumentException();
        this.warmupNanos = warmupNanos;
        this.measurementNanos = measurementNanos;
    }

    public void start(long nowNanos) {
        clearAggregates();
        phase = warmupNanos == 0 ? BenchmarkPhase.MEASURING : BenchmarkPhase.WARMING_UP;
        phaseStarted = nowNanos;
    }

    public void update(FrameTimeSnapshot sample, long nowNanos, long epochMillis) {
        if (phase == BenchmarkPhase.WARMING_UP) {
            if (nowNanos - phaseStarted < warmupNanos) return;
            phase = BenchmarkPhase.MEASURING;
            phaseStarted = nowNanos;
        }
        if (phase != BenchmarkPhase.MEASURING) return;
        if (valid(sample)) add(sample);
        if (nowNanos - phaseStarted >= measurementNanos) complete(epochMillis, sample == null ? 0 : sample.targetFps());
    }

    public void cancel() {
        if (phase == BenchmarkPhase.WARMING_UP || phase == BenchmarkPhase.MEASURING) phase = BenchmarkPhase.CANCELLED;
    }

    public boolean active() {
        return phase == BenchmarkPhase.WARMING_UP || phase == BenchmarkPhase.MEASURING;
    }

    public BenchmarkSnapshot snapshot(long nowNanos) {
        double progress = switch (phase) {
            case WARMING_UP -> fraction(nowNanos - phaseStarted, warmupNanos);
            case MEASURING -> fraction(nowNanos - phaseStarted, measurementNanos);
            case COMPLETED -> 1.0;
            default -> 0.0;
        };
        return new BenchmarkSnapshot(phase, progress, Optional.ofNullable(result));
    }

    private void add(FrameTimeSnapshot sample) {
        observations++;
        fpsTotal += sample.averageFps();
        stableMinimum = Math.min(stableMinimum, sample.stableMinimumFps());
        onePercentLow = Math.min(onePercentLow, sample.onePercentLowFps());
        frameTimeTotal += sample.averageFrameTimeMillis();
        p95Maximum = Math.max(p95Maximum, sample.p95FrameTimeMillis());
        if (sample.targetStatus() == TargetStatus.MEETING_TARGET) targetMet++;
    }

    private void complete(long epochMillis, int targetFps) {
        phase = BenchmarkPhase.COMPLETED;
        if (observations == 0) {
            result = new BenchmarkResult(epochMillis, 0, 0, 0, 0, 0, 0, 0, targetFps);
            return;
        }
        result = new BenchmarkResult(epochMillis, observations, fpsTotal / observations,
            stableMinimum, onePercentLow, frameTimeTotal / observations, p95Maximum,
            targetMet * 100.0 / observations, targetFps);
    }

    private void clearAggregates() {
        observations = targetMet = 0;
        fpsTotal = frameTimeTotal = p95Maximum = 0;
        stableMinimum = onePercentLow = Double.POSITIVE_INFINITY;
        result = null;
    }

    private static boolean valid(FrameTimeSnapshot sample) {
        return sample != null && sample.targetStatus() != TargetStatus.WARMING_UP
            && Double.isFinite(sample.averageFps()) && sample.averageFps() > 0;
    }

    private static double fraction(long elapsed, long duration) {
        return duration == 0 ? 1.0 : Math.max(0.0, Math.min(1.0, (double) elapsed / duration));
    }
}
