package dev.lumina.engine.common.benchmark;

import static org.junit.jupiter.api.Assertions.*;
import dev.lumina.engine.common.QualityProfile;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BenchmarkComparisonTest {
    @TempDir Path temp;

    @Test void computesAbsoluteAndPercentageDifferences() {
        BenchmarkComparison value = BenchmarkComparison.compare(record("a", result(100, 50, 20)), record("b", result(120, 60, 18)));
        assertEquals(20, value.averageFpsDelta());
        assertEquals(20, value.averageFpsPercent());
        assertEquals(10, value.onePercentLowDelta());
        assertEquals(-10, value.p95FrameTimePercent());
    }

    @Test void storesHistoryLocallyAndRecoversFromCorruption() throws Exception {
        BenchmarkHistoryStore store = new BenchmarkHistoryStore(temp.resolve("history.json"));
        store.append(record("a", result(100, 50, 20)));
        store.append(record("b", result(120, 60, 18)));
        assertEquals(2, store.load().size());
        java.nio.file.Files.writeString(temp.resolve("history.json"), "not json");
        assertTrue(store.load().isEmpty());
    }

    private static BenchmarkRecord record(String id, BenchmarkResult result) {
        return new BenchmarkRecord(id, id, QualityProfile.BALANCED, "shader-on", result);
    }
    private static BenchmarkResult result(double avg, double low, double p95) {
        return new BenchmarkResult(1, 60, avg, low, low, 10, p95, 90, 60);
    }
}
