package dev.lumina.engine.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ControlCenterSessionTest {
    @TempDir
    Path temporaryDirectory;

    private Path configPath;
    private ConfigStore store;
    private ControlCenterSession session;

    @BeforeEach
    void setUp() throws Exception {
        configPath = temporaryDirectory.resolve("lumina_engine.json");
        store = new ConfigStore(configPath);
        session = ControlCenterSession.open(store, new FakePlatformAdapter());
    }

    @Test
    void savePersistsEditedValues() throws Exception {
        session.setProfile(QualityProfile.QUALITY);
        session.setTargetFps(144);
        session.save();

        LuminaConfig reloaded = store.load().config();
        assertEquals(QualityProfile.QUALITY, reloaded.profile());
        assertEquals(144, reloaded.targetFps());
        assertFalse(reloaded.adaptiveOptimizationEnabled());
    }

    @Test
    void cancelRestoresLastSavedValues() throws Exception {
        session.setProfile(QualityProfile.CINEMATIC);
        session.setTargetFps(120);
        session.save();
        session.setProfile(QualityProfile.PERFORMANCE);
        session.setTargetFps(30);

        session.cancel();

        assertEquals(QualityProfile.CINEMATIC, session.profile());
        assertEquals(120, session.targetFps());
        LuminaConfig reloaded = store.load().config();
        assertEquals(QualityProfile.CINEMATIC, reloaded.profile());
        assertEquals(120, reloaded.targetFps());
    }

    @Test
    void restoreDefaultsResetsEditableValues() {
        session.setProfile(QualityProfile.CUSTOM);
        session.setTargetFps(240);

        session.restoreDefaults();

        assertEquals(QualityProfile.BALANCED, session.profile());
        assertEquals(60, session.targetFps());
        assertFalse(session.adaptiveOptimizationEnabled());
    }

    @Test
    void navigationExposesConfigurationAndDiagnostics() {
        assertEquals(
            java.util.List.of(ControlCenterSection.CONFIGURATION, ControlCenterSection.DIAGNOSTICS),
            session.sections()
        );
        assertEquals(ControlCenterSection.CONFIGURATION, session.selectedSection());

        session.selectSection(ControlCenterSection.DIAGNOSTICS);

        assertEquals(ControlCenterSection.DIAGNOSTICS, session.selectedSection());
        assertEquals(6, session.diagnostics().mods().size());
    }

    private static final class FakePlatformAdapter implements PlatformAdapter {
        private final Map<String, String> installedMods = Map.of("fabric-api", "test-version", "iris", "test-version");

        @Override
        public Path configDirectory() {
            return Path.of("config");
        }

        @Override
        public Optional<String> installedModVersion(String modId) {
            return Optional.ofNullable(installedMods.get(modId));
        }
    }
}
