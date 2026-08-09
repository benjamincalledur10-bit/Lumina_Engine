package dev.lumina.engine.common;

import java.nio.file.Path;
import java.util.Optional;

public interface PlatformAdapter {
    Path configDirectory();

    Optional<String> installedModVersion(String modId);
}
