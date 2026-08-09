package dev.lumina.engine.common;

import java.util.List;

public record DiagnosticResult(List<ModStatus> mods) {
    public DiagnosticResult {
        mods = List.copyOf(mods);
    }
}
