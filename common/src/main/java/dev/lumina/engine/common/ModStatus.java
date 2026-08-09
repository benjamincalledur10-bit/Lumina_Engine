package dev.lumina.engine.common;

public record ModStatus(String id, String displayName, boolean installed, String version) {
    public ModStatus {
        version = installed && version != null ? version : "not installed";
    }
}
