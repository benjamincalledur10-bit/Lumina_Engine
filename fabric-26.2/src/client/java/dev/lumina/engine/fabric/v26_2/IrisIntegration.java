package dev.lumina.engine.fabric.v26_2;

import dev.lumina.engine.common.iris.IrisStatus;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

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
            Minecraft.getInstance().setScreenAndShow(IrisPublicApiAccess.createShaderScreen(parent));
        }
    }

    void setShadersEnabled(boolean enabled) {
        if (installedVersion.isPresent()) IrisPublicApiAccess.setShadersEnabled(enabled);
    }
}
