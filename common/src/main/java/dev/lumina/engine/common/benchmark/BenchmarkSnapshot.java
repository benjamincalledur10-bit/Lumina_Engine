package dev.lumina.engine.common.benchmark;

import java.util.Optional;

public record BenchmarkSnapshot(BenchmarkPhase phase, double progress, Optional<BenchmarkResult> result) {
    public static BenchmarkSnapshot idle() {
        return new BenchmarkSnapshot(BenchmarkPhase.IDLE, 0.0, Optional.empty());
    }
}
