package dev.lumina.engine.fabric.v26_2;

import dev.lumina.engine.common.ConfigStore;
import dev.lumina.engine.common.Diagnostics;
import dev.lumina.engine.common.LuminaConfig;
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
        int targetFps = LuminaConfig.DEFAULT_TARGET_FPS;
        var profile = dev.lumina.engine.common.QualityProfile.BALANCED;

        try {
            ConfigStore.LoadResult result = configStore.load();
            targetFps = result.config().targetFps();
            profile = result.config().profile();
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

        FrameTelemetryRuntime.initialize(targetFps, profile);

        Diagnostics.inspect(platform).mods().forEach(status ->
            LOGGER.info("Dependency diagnostic: {} ({})", status.displayName(), status.version())
        );
        LOGGER.info("Lumina Engine initialized for Minecraft 26.2");
    }
}
