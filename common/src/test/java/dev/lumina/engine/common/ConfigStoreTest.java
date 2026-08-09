package dev.lumina.engine.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigStoreTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void corruptConfigurationIsBackedUpAndReplacedWithDefaults() throws Exception {
        Path configPath = temporaryDirectory.resolve("lumina_engine.json");
        Files.writeString(configPath, "{ definitely not valid JSON");

        ConfigStore.LoadResult result = new ConfigStore(configPath).load();

        assertTrue(result.recoveredFromCorruption());
        assertTrue(Files.exists(result.corruptBackup()));
        assertTrue(Files.exists(configPath));
        assertEquals(QualityProfile.BALANCED, result.config().profile());
        assertEquals(60, result.config().targetFps());
        assertFalse(result.config().adaptiveOptimizationEnabled());
    }
}
