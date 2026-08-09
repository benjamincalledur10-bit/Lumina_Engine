package dev.lumina.engine.common.iris;

import java.util.Optional;
import java.util.function.Supplier;

public final class IrisDiagnostics {
    private IrisDiagnostics() {}

    public static IrisStatus inspect(Optional<String> installedVersion, Supplier<IrisApiView> apiSupplier) {
        if (installedVersion.isEmpty()) {
            return IrisStatus.absent();
        }

        IrisApiView api = apiSupplier.get();
        boolean shadersEnabled = api.shadersEnabled();
        boolean shaderActive = shadersEnabled && api.shaderPackInUse();
        return new IrisStatus(true, installedVersion.get(), shadersEnabled, shaderActive);
    }
}
