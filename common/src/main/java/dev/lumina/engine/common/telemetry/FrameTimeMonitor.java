package dev.lumina.engine.common.telemetry;

import java.util.Arrays;

public final class FrameTimeMonitor {
    public static final int DEFAULT_CAPACITY = 600;
    public static final int DEFAULT_WARMUP_FRAMES = 120;
    public static final long DEFAULT_MAX_FRAME_NANOS = 1_000_000_000L;

    private final long[] samples;
    private final long[] scratch;
    private final int warmupFrames;
    private final long maximumFrameNanos;
    private long previousFrameNanos = Long.MIN_VALUE;
    private int warmupRemaining;
    private int writeIndex;
    private int size;

    public FrameTimeMonitor() {
        this(DEFAULT_CAPACITY, DEFAULT_WARMUP_FRAMES, DEFAULT_MAX_FRAME_NANOS);
    }

    public FrameTimeMonitor(int capacity, int warmupFrames, long maximumFrameNanos) {
        if (capacity < 1 || warmupFrames < 0 || maximumFrameNanos < 1) throw new IllegalArgumentException();
        this.samples = new long[capacity];
        this.scratch = new long[capacity];
        this.warmupFrames = warmupFrames;
        this.warmupRemaining = warmupFrames;
        this.maximumFrameNanos = maximumFrameNanos;
    }

    public boolean recordFrame(long nowNanos) {
        if (previousFrameNanos == Long.MIN_VALUE) {
            previousFrameNanos = nowNanos;
            return false;
        }
        long elapsed = nowNanos - previousFrameNanos;
        previousFrameNanos = nowNanos;
        if (elapsed <= 0 || elapsed > maximumFrameNanos) {
            clearSamples();
            return false;
        }
        if (warmupRemaining > 0) {
            warmupRemaining--;
            return false;
        }
        samples[writeIndex] = elapsed;
        writeIndex = (writeIndex + 1) % samples.length;
        if (size < samples.length) size++;
        return true;
    }

    public FrameTimeSnapshot snapshot(int targetFps) {
        if (size == 0) return FrameTimeSnapshot.warmingUp(0, targetFps);
        long total = 0;
        for (int i = 0; i < size; i++) {
            scratch[i] = samples[i];
            total += samples[i];
        }
        Arrays.sort(scratch, 0, size);
        double averageNanos = (double) total / size;
        long p95Nanos = scratch[Math.max(0, (int) Math.ceil(size * 0.95) - 1)];
        int lowCount = Math.max(1, (int) Math.ceil(size * 0.01));
        long lowTotal = 0;
        for (int i = size - lowCount; i < size; i++) lowTotal += scratch[i];
        double averageFps = 1_000_000_000.0 / averageNanos;
        double stableMinimumFps = 1_000_000_000.0 / p95Nanos;
        double onePercentLowFps = 1_000_000_000.0 / ((double) lowTotal / lowCount);
        TargetStatus status = averageFps >= targetFps ? TargetStatus.MEETING_TARGET : TargetStatus.BELOW_TARGET;
        return new FrameTimeSnapshot(size, averageFps, stableMinimumFps, onePercentLowFps,
            averageNanos / 1_000_000.0, p95Nanos / 1_000_000.0, targetFps, status);
    }

    public int sampleCount() { return size; }

    private void clearSamples() {
        size = 0;
        writeIndex = 0;
        warmupRemaining = warmupFrames;
    }
}
