package dev.lumina.engine.common.compat;

import java.util.Map;

public final class CompatibilityAdvisor {
    private static final Map<String, Pair> VERIFIED = Map.of(
        "1.21.11", new Pair("1.10.7+1.21.11-fabric", "mc1.21.11-0.8.7-fabric"),
        "26.2", new Pair("1.11.2+26.2-fabric", "mc26.2-0.9.1-fabric")
    );

    private CompatibilityAdvisor() {}

    public static CompatibilityAssessment assess(String minecraft, String iris, String sodium) {
        Pair expected = VERIFIED.get(minecraft);
        if (expected == null || missing(iris) || missing(sodium)) {
            return new CompatibilityAssessment(CompatibilityLevel.UNKNOWN_VERSION,
                expected == null ? "" : expected.iris, expected == null ? "" : expected.sodium);
        }
        boolean expectedIris = matches(iris, expected.irisVersion(), expected.minecraftToken());
        boolean expectedSodium = sodium.contains(expected.sodiumVersion()) && sodium.contains(expected.minecraftToken());
        if (expectedIris && expectedSodium) {
            return new CompatibilityAssessment(CompatibilityLevel.VERIFIED_BY_LUMINA, expected.iris, expected.sodium);
        }
        boolean knownWrongPair = expectedIris != expectedSodium;
        return new CompatibilityAssessment(knownWrongPair
            ? CompatibilityLevel.INCOMPATIBLE
            : CompatibilityLevel.COMPATIBLE_UNTESTED, expected.iris, expected.sodium);
    }

    private static boolean missing(String value) {
        return value == null || value.isBlank() || "not installed".equalsIgnoreCase(value);
    }

    private static boolean matches(String actual, String version, String minecraftToken) {
        return actual.startsWith(version) && actual.contains(minecraftToken);
    }

    private record Pair(String iris, String sodium) {
        String irisVersion() { return iris.substring(0, iris.indexOf('+')); }
        String sodiumVersion() {
            int start = sodium.indexOf('-', 2) + 1;
            return sodium.substring(start, sodium.lastIndexOf("-fabric"));
        }
        String minecraftToken() { return iris.contains("1.21.11") ? "1.21.11" : "26.2"; }
    }
}
