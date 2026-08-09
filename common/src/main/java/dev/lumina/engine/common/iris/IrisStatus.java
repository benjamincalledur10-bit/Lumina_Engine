package dev.lumina.engine.common.iris;

public record IrisStatus(boolean installed, String version, boolean shadersEnabled, boolean shaderActive) {
    public IrisStatus {
        version = installed && version != null ? version : "not installed";
        if (!installed && (shadersEnabled || shaderActive)) {
            throw new IllegalArgumentException("Iris cannot expose shader state when it is not installed");
        }
    }

    public static IrisStatus absent() {
        return new IrisStatus(false, null, false, false);
    }
}
