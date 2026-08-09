package dev.lumina.engine.common;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public final class ControlCenterSession {
    private final ConfigStore configStore;
    private final DiagnosticResult diagnostics;
    private LuminaConfig savedConfig;
    private LuminaConfig draftConfig;
    private ControlCenterSection selectedSection = ControlCenterSection.CONFIGURATION;

    private ControlCenterSession(ConfigStore configStore, LuminaConfig config, DiagnosticResult diagnostics) {
        this.configStore = configStore;
        this.savedConfig = config.copy();
        this.draftConfig = config.copy();
        this.diagnostics = diagnostics;
    }

    public static ControlCenterSession open(ConfigStore configStore, PlatformAdapter platform) throws IOException {
        Objects.requireNonNull(configStore, "configStore");
        Objects.requireNonNull(platform, "platform");
        return new ControlCenterSession(configStore, configStore.load().config(), Diagnostics.inspect(platform));
    }

    public static ControlCenterSession withDefaults(ConfigStore configStore, PlatformAdapter platform) {
        Objects.requireNonNull(configStore, "configStore");
        Objects.requireNonNull(platform, "platform");
        return new ControlCenterSession(configStore, LuminaConfig.defaults(), Diagnostics.inspect(platform));
    }

    public QualityProfile profile() {
        return draftConfig.profile();
    }

    public void setProfile(QualityProfile profile) {
        draftConfig.setProfile(profile);
    }

    public int targetFps() {
        return draftConfig.targetFps();
    }

    public void setTargetFps(int targetFps) {
        draftConfig.setTargetFps(targetFps);
    }

    public boolean adaptiveOptimizationEnabled() {
        return draftConfig.adaptiveOptimizationEnabled();
    }

    public DiagnosticResult diagnostics() {
        return diagnostics;
    }

    public List<ControlCenterSection> sections() {
        return List.of(ControlCenterSection.CONFIGURATION, ControlCenterSection.DIAGNOSTICS);
    }

    public ControlCenterSection selectedSection() {
        return selectedSection;
    }

    public void selectSection(ControlCenterSection section) {
        if (!sections().contains(Objects.requireNonNull(section, "section"))) {
            throw new IllegalArgumentException("Unknown Control Center section: " + section);
        }
        selectedSection = section;
    }

    public void restoreDefaults() {
        draftConfig = LuminaConfig.defaults();
    }

    public void cancel() {
        draftConfig = savedConfig.copy();
    }

    public void save() throws IOException {
        configStore.save(draftConfig);
        savedConfig = draftConfig.copy();
    }
}
