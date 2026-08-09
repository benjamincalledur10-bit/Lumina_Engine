package dev.lumina.engine.fabric.v1_21_11;

import dev.lumina.engine.common.iris.IrisStatus;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

final class IrisIntegration {
    private final Optional<String> installedVersion;
    private final IrisStatus status;

    IrisIntegration(FabricPlatformAdapter platform) {
        installedVersion = platform.installedModVersion("iris");
        status = installedVersion.isPresent()
            ? IrisPublicApiAccess.status(installedVersion.get())
            : IrisStatus.absent();
    }

    IrisStatus status() {
        return status;
    }

    void openShaderScreen(Screen parent) {
        if (installedVersion.isPresent()) {
            MinecraftClient.getInstance().setScreen(IrisPublicApiAccess.createShaderScreen(parent));
        }
    }

    void setShadersEnabled(boolean enabled) {
        if (installedVersion.isPresent()) IrisPublicApiAccess.setShadersEnabled(enabled);
    }
}
