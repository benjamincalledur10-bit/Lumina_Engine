# Changelog

All notable changes to Lumina Engine are documented in this file.

## [0.0.1-alpha.2] - Unreleased

### Added

- Persistent actionable recommendations with suggested profile, planned
  changes, timestamp, cooldown remaining, and dismissal.
- User-guided benchmark sessions with warmup, measurement, progress,
  cancellation, context invalidation, local history, and before/after metrics.
- Iris/Sodium compatibility advisor with a non-blocking verified-pair matrix.
- Optional four-position performance HUD, disabled by default.
- Confirmed user-authorized Iris shader enable/disable controls through the
  public API.
- Coordinated manual preset guidance for Event Horizon and Lumina Lite.
- Mexican Spanish translations.

### Safety

- Adaptive mode remains disabled and no recommendation is applied
  automatically.
- Benchmark history stays local and contains no personal information.

## [0.0.1-alpha.1] - 2026-08-09

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
