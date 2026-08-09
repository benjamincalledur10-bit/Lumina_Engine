package dev.lumina.engine.common.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class CompatibilityAdvisorTest {
    @Test void recognizesBothVerifiedPairs() {
        assertEquals(CompatibilityLevel.VERIFIED_BY_LUMINA, CompatibilityAdvisor.assess("1.21.11",
            "1.10.7+1.21.11-fabric", "mc1.21.11-0.8.7-fabric").level());
        assertEquals(CompatibilityLevel.VERIFIED_BY_LUMINA, CompatibilityAdvisor.assess("26.2",
            "1.11.2+26.2-fabric", "mc26.2-0.9.1-fabric").level());
    }

    @Test void distinguishesUnknownUntestedAndKnownMismatch() {
        assertEquals(CompatibilityLevel.UNKNOWN_VERSION,
            CompatibilityAdvisor.assess("26.2", null, null).level());
        assertEquals(CompatibilityLevel.COMPATIBLE_UNTESTED,
            CompatibilityAdvisor.assess("26.2", "1.12.0", "0.10.0").level());
        assertEquals(CompatibilityLevel.INCOMPATIBLE,
            CompatibilityAdvisor.assess("26.2", "1.11.2+26.2-fabric", "0.9.2").level());
    }
}
