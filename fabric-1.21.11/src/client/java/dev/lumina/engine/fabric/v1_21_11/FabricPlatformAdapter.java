package dev.lumina.engine.fabric.v1_21_11;

import dev.lumina.engine.common.PlatformAdapter;
import java.nio.file.Path;
import java.util.Optional;
import net.fabricmc.loader.api.FabricLoader;

final class FabricPlatformAdapter implements PlatformAdapter {
    private final FabricLoader loader = FabricLoader.getInstance();

    @Override
    public Path configDirectory() {
        return loader.getConfigDir();
    }

    @Override
    public Optional<String> installedModVersion(String modId) {
        return loader.getModContainer(modId)
            .map(container -> container.getMetadata().getVersion().getFriendlyString());
    }
}
