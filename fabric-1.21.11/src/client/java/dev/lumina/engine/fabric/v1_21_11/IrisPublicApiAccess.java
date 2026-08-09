package dev.lumina.engine.fabric.v1_21_11;

import dev.lumina.engine.common.iris.IrisApiView;
import dev.lumina.engine.common.iris.IrisDiagnostics;
import dev.lumina.engine.common.iris.IrisStatus;
import java.util.Optional;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.client.gui.screen.Screen;

final class IrisPublicApiAccess {
    private IrisPublicApiAccess() {}

    static IrisStatus status(String version) {
        IrisApi api = IrisApi.getInstance();
        return IrisDiagnostics.inspect(Optional.of(version), () -> new IrisApiView() {
            @Override
            public boolean shadersEnabled() {
                return api.getConfig().areShadersEnabled();
            }

            @Override
            public boolean shaderPackInUse() {
                return api.isShaderPackInUse();
            }
        });
    }

    static Screen createShaderScreen(Screen parent) {
        return (Screen) IrisApi.getInstance().openMainIrisScreenObj(parent);
    }
}
