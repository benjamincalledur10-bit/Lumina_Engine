package dev.lumina.engine.common;

import java.util.List;

public final class Diagnostics {
    private static final List<DetectedMod> KNOWN_MODS = List.of(
        new DetectedMod("fabric-api", "Fabric API"),
        new DetectedMod("iris", "Iris"),
        new DetectedMod("sodium", "Sodium"),
        new DetectedMod("distanthorizons", "Distant Horizons"),
        new DetectedMod("yet_another_config_lib_v3", "YACL"),
        new DetectedMod("modmenu", "Mod Menu")
    );

    private Diagnostics() {}

    public static DiagnosticResult inspect(PlatformAdapter platform) {
        List<ModStatus> statuses = KNOWN_MODS.stream().map(mod -> {
            var version = platform.installedModVersion(mod.id());
            return new ModStatus(mod.id(), mod.displayName(), version.isPresent(), version.orElse(null));
        }).toList();
        return new DiagnosticResult(statuses);
    }

    private record DetectedMod(String id, String displayName) {}
}
