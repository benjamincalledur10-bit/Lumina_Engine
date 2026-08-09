package dev.lumina.engine.common.iris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class IrisDiagnosticsTest {
    @Test
    void irisAbsentDoesNotLoadItsApi() {
        IrisStatus status = IrisDiagnostics.inspect(Optional.empty(), () -> {
            throw new AssertionError("Iris API must not be loaded when Iris is absent");
        });

        assertFalse(status.installed());
        assertEquals("not installed", status.version());
        assertFalse(status.shadersEnabled());
        assertFalse(status.shaderActive());
    }

    @Test
    void irisPresentWithCompiledShaderReportsAllStates() {
        IrisStatus status = IrisDiagnostics.inspect(
            Optional.of("1.10.7"),
            () -> new FakeIrisApi(true, true)
        );

        assertTrue(status.installed());
        assertEquals("1.10.7", status.version());
        assertTrue(status.shadersEnabled());
        assertTrue(status.shaderActive());
    }

    @Test
    void disabledShadersCannotBeReportedAsActive() {
        IrisStatus status = IrisDiagnostics.inspect(
            Optional.of("1.11.2"),
            () -> new FakeIrisApi(false, true)
        );

        assertTrue(status.installed());
        assertFalse(status.shadersEnabled());
        assertFalse(status.shaderActive());
    }

    @Test
    void enabledShaderThatDidNotCompileIsNotActive() {
        IrisStatus status = IrisDiagnostics.inspect(
            Optional.of("1.11.2"),
            () -> new FakeIrisApi(true, false)
        );

        assertTrue(status.installed());
        assertTrue(status.shadersEnabled());
        assertFalse(status.shaderActive());
    }

    private record FakeIrisApi(boolean shadersEnabled, boolean shaderPackInUse) implements IrisApiView {}
}
