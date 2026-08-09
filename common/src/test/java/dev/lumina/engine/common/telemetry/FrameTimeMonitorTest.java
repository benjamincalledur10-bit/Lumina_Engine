package dev.lumina.engine.common.telemetry;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class FrameTimeMonitorTest {
    @Test void ignoresWarmup() {
        FrameTimeMonitor monitor = new FrameTimeMonitor(4, 2, 1_000_000_000L);
        monitor.recordFrame(0); monitor.recordFrame(10_000_000); monitor.recordFrame(20_000_000);
        assertEquals(0, monitor.sampleCount());
        monitor.recordFrame(30_000_000);
        assertEquals(1, monitor.sampleCount());
    }

    @Test void circularBufferKeepsItsFixedCapacity() {
        FrameTimeMonitor monitor = new FrameTimeMonitor(3, 0, 1_000_000_000L);
        long now = 0; monitor.recordFrame(now);
        for (int i = 0; i < 5; i++) monitor.recordFrame(now += 10_000_000);
        assertEquals(3, monitor.sampleCount());
    }

    @Test void invalidAndPausedSamplesResetTheWindow() {
        FrameTimeMonitor monitor = new FrameTimeMonitor(4, 0, 100_000_000L);
        monitor.recordFrame(10); monitor.recordFrame(20); assertEquals(1, monitor.sampleCount());
        monitor.recordFrame(20); assertEquals(0, monitor.sampleCount());
        monitor.recordFrame(200_000_020); assertEquals(0, monitor.sampleCount());
    }

    @Test void calculatesPercentilesAndOnePercentLow() {
        FrameTimeMonitor monitor = new FrameTimeMonitor(100, 0, 1_000_000_000L);
        long now = 0; monitor.recordFrame(now);
        for (int i = 0; i < 95; i++) monitor.recordFrame(now += 10_000_000);
        for (int i = 0; i < 5; i++) monitor.recordFrame(now += 20_000_000);
        FrameTimeSnapshot snapshot = monitor.snapshot(60);
        assertEquals(10.0, snapshot.p95FrameTimeMillis(), 0.001);
        assertEquals(50.0, snapshot.onePercentLowFps(), 0.001);
        assertEquals(100.0, snapshot.stableMinimumFps(), 0.001);
    }

    @Test void comparesAverageFpsAgainstTarget() {
        FrameTimeMonitor monitor = new FrameTimeMonitor(4, 0, 1_000_000_000L);
        monitor.recordFrame(0); monitor.recordFrame(20_000_000);
        assertEquals(TargetStatus.BELOW_TARGET, monitor.snapshot(60).targetStatus());
        assertEquals(TargetStatus.MEETING_TARGET, monitor.snapshot(30).targetStatus());
    }

    @Test void resetDiscardsInheritedFrameData() {
        FrameTimeMonitor monitor = new FrameTimeMonitor(4, 0, 1_000_000_000L);
        monitor.recordFrame(0);
        monitor.recordFrame(10_000_000);
        monitor.reset();
        assertEquals(0, monitor.sampleCount());
        assertEquals(TargetStatus.WARMING_UP, monitor.snapshot(60).targetStatus());
    }
}
