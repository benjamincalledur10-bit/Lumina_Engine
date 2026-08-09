# Changelog

All notable changes to Lumina Engine are documented in this file.

## [0.0.1-alpha.1] - Unreleased

### Added

- Separate client-only Fabric JARs for Minecraft 1.21.11/Java 21 and Minecraft
  26.2/Java 25.
- Shared quality profiles, validated configuration, corrupt-file recovery, and
  local dependency diagnostics.
- YACL Control Center integration through the optional Mod Menu API, with
  equivalent English and Spanish translations.
- Read-only Iris status and access to Iris's public shader-pack screen.
- In-memory frame-time metrics, adaptive quality recommendations, and one-level
  adjustment plan previews.
- Unit tests and a dual-version GitHub Actions build matrix.

### Compatibility

- Minecraft 1.21.11: Iris 1.10.7 with Sodium 0.8.7.
- Minecraft 26.2: Iris 1.11.2 with Sodium 0.9.1.

### Limitations

- Adaptive mode remains disabled; recommendations never change settings.
- No direct Event Horizon or Lumina Lite integration is included yet.
- No automatic downloads, telemetry, benchmarks, or persistent performance
  history are included.
- This alpha remains unpublished until both JARs pass manual shader-pack tests.
