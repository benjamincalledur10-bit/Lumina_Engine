package dev.lumina.engine.common.benchmark;

import dev.lumina.engine.common.QualityProfile;

public record BenchmarkRecord(String id, String label, QualityProfile profile, String renderContext,
                              BenchmarkResult result) {}
