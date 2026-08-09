package dev.lumina.engine.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ConfigStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path configPath;

    public ConfigStore(Path configPath) {
        this.configPath = configPath;
    }

    public LoadResult load() throws IOException {
        if (Files.notExists(configPath)) {
            LuminaConfig defaults = LuminaConfig.defaults();
            save(defaults);
            return new LoadResult(defaults, false, null);
        }

        try {
            String json = Files.readString(configPath, StandardCharsets.UTF_8);
            SerializedConfig serialized = GSON.fromJson(json, SerializedConfig.class);
            if (serialized == null || serialized.profile == null) {
                throw new JsonParseException("Missing required configuration values");
            }
            LuminaConfig config = new LuminaConfig(
                QualityProfile.fromString(serialized.profile),
                serialized.targetFps,
                serialized.adaptiveOptimizationEnabled
            );
            return new LoadResult(config, false, null);
        } catch (JsonParseException | IllegalArgumentException | IllegalStateException exception) {
            Path backup = nextCorruptBackupPath();
            Files.move(configPath, backup);
            LuminaConfig defaults = LuminaConfig.defaults();
            save(defaults);
            return new LoadResult(defaults, true, backup);
        }
    }

    public void save(LuminaConfig config) throws IOException {
        Path parent = configPath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        SerializedConfig serialized = new SerializedConfig(
            config.profile().serializedName(),
            config.targetFps(),
            config.adaptiveOptimizationEnabled()
        );
        Path temporaryFile = configPath.resolveSibling(configPath.getFileName() + ".tmp");
        Files.writeString(temporaryFile, GSON.toJson(serialized) + System.lineSeparator(), StandardCharsets.UTF_8);
        try {
            Files.move(
                temporaryFile,
                configPath,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            );
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporaryFile, configPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Path nextCorruptBackupPath() {
        Path candidate = configPath.resolveSibling(configPath.getFileName() + ".corrupt");
        int suffix = 1;
        while (Files.exists(candidate)) {
            candidate = configPath.resolveSibling(configPath.getFileName() + ".corrupt." + suffix++);
        }
        return candidate;
    }

    public record LoadResult(LuminaConfig config, boolean recoveredFromCorruption, Path corruptBackup) {}

    private record SerializedConfig(String profile, int targetFps, boolean adaptiveOptimizationEnabled) {}
}
