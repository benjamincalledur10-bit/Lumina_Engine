package dev.lumina.engine.common.benchmark;

import static org.junit.jupiter.api.Assertions.*;

import dev.lumina.engine.common.telemetry.FrameTimeSnapshot;
import dev.lumina.engine.common.telemetry.TargetStatus;
import org.junit.jupiter.api.Test;

class GuidedBenchmarkSessionTest {
    @Test void followsWarmupMeasurementAndCompletion() {
        GuidedBenchmarkSession session = new GuidedBenchmarkSession(10, 60);
        session.start(0);
        session.update(sample(70, 55, 50, 14, 18, TargetStatus.MEETING_TARGET), 9, 1);
        assertEquals(BenchmarkPhase.WARMING_UP, session.snapshot(9).phase());
        session.update(sample(70, 55, 50, 14, 18, TargetStatus.MEETING_TARGET), 10, 1);
        session.update(sample(60, 45, 40, 16, 22, TargetStatus.BELOW_TARGET), 70, 2);
        BenchmarkResult result = session.snapshot(70).result().orElseThrow();
        assertEquals(2, result.observations());
        assertEquals(65, result.averageFps());
        assertEquals(45, result.stableMinimumFps());
        assertEquals(40, result.onePercentLowFps());
        assertEquals(50, result.targetMetPercent());
    }

    @Test void cancellationIsSafeAndContextReusable() {
        GuidedBenchmarkSession session = new GuidedBenchmarkSession(0, 10);
        session.start(0);
        session.cancel();
        assertFalse(session.active());
        assertEquals(BenchmarkPhase.CANCELLED, session.snapshot(5).phase());
        session.start(10);
        assertTrue(session.active());
    }

    @Test void invalidSamplesAreIgnored() {
        GuidedBenchmarkSession session = new GuidedBenchmarkSession(0, 10);
        session.start(0);
        session.update(FrameTimeSnapshot.warmingUp(0, 60), 10, 1);
        assertEquals(0, session.snapshot(10).result().orElseThrow().observations());
    }

    private static FrameTimeSnapshot sample(double avg, double stable, double low, double frame,
                                              double p95, TargetStatus status) {
        return new FrameTimeSnapshot(600, avg, stable, low, frame, p95, 60, status);
    }
}
