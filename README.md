# Lumina Engine

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![Fabric](https://img.shields.io/badge/Mod%20loader-Fabric-dbd0b4.svg)](https://fabricmc.net/)

Lumina Engine is a client-only Fabric mod that provides profiles,
compatibility diagnostics, configuration management, and the foundation for
future adaptive tuning across the Lumina ecosystem:

- Lumina Shader – Event Horizon
- Lumina Shader Lite
- Lumina Materials (planned)
- Lumina Realism modpack (planned)

Lumina Engine does not claim to increase FPS by itself. Deep rendering
optimizations belong in the Lumina shaders; the engine coordinates compatible
settings and will make measured, user-authorized adjustments in future releases.

> [!IMPORTANT]
> The project is in alpha preparation. Version `0.0.1-alpha.1` is not a stable
> release and must be tested before publication.

## Supported targets

The same feature set is built as two separate JARs because Minecraft 26.2 is
unobfuscated and requires a different Loom pipeline and Java toolchain.

| Target | Java | Loom plugin | Output |
| --- | ---: | --- | --- |
| Minecraft 1.21.11 | 21 | `net.fabricmc.fabric-loom-remap` | `lumina-engine-fabric-1.21.11-*.jar` |
| Minecraft 26.2 | 25 | `net.fabricmc.fabric-loom` | `lumina-engine-fabric-26.2-*.jar` |

## Current foundation

- Client-only Fabric entrypoints for both targets
- Shared `Performance`, `Balanced`, `Quality`, `Cinematic`, and `Custom` profiles
- Default `Balanced` profile with a 60 FPS target
- Valid target range of 30–240 FPS
- Adaptive optimization disabled by default
- Atomic JSON configuration writes and corrupt-file recovery
- Local dependency/version diagnostics without telemetry
- Detection of Fabric API, YACL, Mod Menu, Iris, Sodium, and Distant Horizons
- YACL-based Lumina Control Center available through Mod Menu
- English and Spanish interface translations
- Local, read-only frame-time metrics with warmup and invalid-sample handling
- Read-only adaptive quality recommendations with hysteresis and cooldowns
- One-level quality adjustment plan previews that are never applied automatically
- Unit tests for shared, Minecraft-independent behavior

No settings are changed automatically. Lumina Engine never downloads mods or
shaders, verifies memberships, or sends telemetry.

## Project layout

```text
Lumina_Engine/
├── common/           # Minecraft-independent models, config, diagnostics, tests
├── fabric-1.21.11/   # Java 21, remapped/obfuscated Fabric target
├── fabric-26.2/      # Java 25, unobfuscated Fabric target
├── .github/workflows/
├── README.md
└── LICENSE
```

The platform modules depend on `common` and package its compiled classes into
each target JAR. Minecraft/Fabric types are kept behind small platform adapters.
The 26.2 module makes no direct OpenGL calls so a future Vulkan backend remains
possible.

## Dependencies

Pinned development versions:

| Dependency | 1.21.11 | 26.2 | Classification |
| --- | --- | --- | --- |
| Fabric Loader | 0.19.3 | 0.19.3 | Required |
| Fabric API | 0.141.6+1.21.11 | 0.156.0+26.2 | Required |
| YACL | 3.8.2+1.21.11-fabric | 3.9.6+26.2-fabric | Required |
| Mod Menu | 17.0.1-beta.1 | 20.0.1 | Recommended |
| Iris | 1.10.7+1.21.11-fabric | 1.11.2+26.2-fabric | Recommended |
| Sodium | mc1.21.11-0.8.7-fabric | mc26.2-0.9.1-fabric | Recommended |
| Distant Horizons | Detected at runtime | Detected at runtime | Optional |

Iris, Sodium, Mod Menu, and Distant Horizons are not required for startup.
The optional Mod Menu integration uses its public API. Iris is an optional
compile-time dependency used exclusively through its public
`net.irisshaders.iris.api.v0` API and is not included in Lumina Engine's JARs.
Sodium and Distant Horizons are detected only through Fabric Loader.

## Lumina Control Center

When Mod Menu is installed, select **Lumina Engine** and open its configuration
screen. The Control Center currently provides:

- a selector for all five quality profiles;
- a target FPS slider from 30 to 240;
- save, cancel, per-option reset, and complete default restoration behavior;
- a read-only diagnostics category with detected dependency versions;
- a manual diagnostics refresh that preserves unsaved profile and FPS changes;
- read-only Iris state showing whether shaders are loaded and whether a shader
  pack is actively compiled and in use;
- a button that opens Iris's public shader-pack screen when Iris is installed;
- English and Spanish translations selected by Minecraft's language setting.

Adaptive optimization is visible but deliberately unavailable. The values saved
by this screen are configuration targets only: they do not modify Minecraft,
Iris, Sodium, or shader settings yet.

The diagnostics panel also displays local frame-time statistics, a read-only
quality recommendation, its reason, and a preview of the next profile and
adjustment domains. Measurements stay in memory, are never transmitted or
stored as history, and reset when relevant client context changes.

The Iris bridge uses only `net.irisshaders.iris.api.v0`. It is loaded lazily,
so Iris remains optional, and it deliberately does not inspect Iris internals,
shader-pack files, or the selected pack's name.

## Build and test

Install JDK 25, then run the complete validation from the repository root:

```bash
./gradlew clean validateAll
```

Individual targets can be built with:

```bash
./gradlew :fabric-1.21.11:build
./gradlew :fabric-26.2:build
./gradlew :common:test
```

The two distributable JARs are written to each platform module's `build/libs/`:

- `fabric-1.21.11/build/libs/lumina-engine-fabric-1.21.11-0.0.1-alpha.1.jar`
- `fabric-26.2/build/libs/lumina-engine-fabric-26.2-0.0.1-alpha.1.jar`

Files whose names contain `sources` are development artifacts and must not be
installed. Iris and Sodium are optional external mods and are not bundled.

## Configuration

At first client launch, Lumina Engine writes `config/lumina_engine.json`. If the
file cannot be parsed or contains invalid values, it is preserved with a
`.corrupt` suffix and replaced with safe defaults. Configuration writes use a
temporary file and an atomic move whenever the filesystem supports it.

## Alpha limitations

- Recommendations and adjustment plans are informational only; adaptive mode
  remains disabled and cannot apply them.
- Lumina Engine does not alter Minecraft, Iris, Sodium, Distant Horizons, or
  shader settings.
- Event Horizon and Lumina Lite are not identified by name and have no direct
  integration yet because the public Iris API does not expose the selected
  shader-pack name.
- There is no benchmark, automatic tuning, shader uniform control, telemetry,
  persistent performance history, or real-client automated smoke test yet.
- A failed configuration write is reported in the log, not in the UI.

Future integrations must use stable APIs or an isolated compatibility
layer—never copied internals or fragile private hooks.

## Contributing

Open an issue before a substantial change, keep pull requests focused, and run
`./gradlew clean validateAll` before submitting code.

## License

Lumina Engine is licensed under the [GNU General Public License v3.0](LICENSE).

Minecraft is a trademark of Microsoft Corporation. This project is not
affiliated with or endorsed by Microsoft or Mojang Studios.

Release history is documented in [CHANGELOG.md](CHANGELOG.md).
