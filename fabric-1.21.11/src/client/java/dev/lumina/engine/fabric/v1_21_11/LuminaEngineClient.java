package dev.lumina.engine.fabric.v1_21_11;

import dev.lumina.engine.common.ConfigStore;
import dev.lumina.engine.common.Diagnostics;
import java.io.IOException;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LuminaEngineClient implements ClientModInitializer {
    public static final String MOD_ID = "lumina_engine";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        FabricPlatformAdapter platform = new FabricPlatformAdapter();
        ConfigStore configStore = new ConfigStore(platform.configDirectory().resolve(MOD_ID + ".json"));

        try {
            ConfigStore.LoadResult result = configStore.load();
            if (result.recoveredFromCorruption()) {
                LOGGER.warn("Recovered corrupt configuration; backup saved to {}", result.corruptBackup());
            }
            LOGGER.info(
                "Configuration loaded: profile={}, targetFps={}, adaptiveOptimization={}",
                result.config().profile().serializedName(),
                result.config().targetFps(),
                result.config().adaptiveOptimizationEnabled()
            );
        } catch (IOException exception) {
            LOGGER.error("Could not load Lumina Engine configuration; using in-memory defaults", exception);
        }

        Diagnostics.inspect(platform).mods().forEach(status ->
            LOGGER.info("Dependency diagnostic: {} ({})", status.displayName(), status.version())
        );
        LOGGER.info("Lumina Engine initialized for Minecraft 1.21.11");
    }
}
