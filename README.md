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
> The project is in early development. There are no stable releases yet.

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
| Sodium | mc1.21.11-0.8.14-beta.2-fabric | mc26.2-0.9.2-alpha.4-fabric | Recommended |
| Distant Horizons | Detected at runtime | Detected at runtime | Optional |

Iris, Sodium, Mod Menu, and Distant Horizons are not required for startup.
Their APIs are not linked or copied.

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

The distributable JARs are written to each platform module's `build/libs/`
directory. Do not use a JAR whose filename contains `sources`.

## Configuration

At first client launch, Lumina Engine writes `config/lumina_engine.json`. If the
file cannot be parsed or contains invalid values, it is preserved with a
`.corrupt` suffix and replaced with safe defaults. Configuration writes use a
temporary file and an atomic move whenever the filesystem supports it.

## Roadmap boundaries

YACL screens, automatic option changes, benchmarking, shader uniforms, direct
shader integration, frame-time measurement, and adaptive FPS control are not
part of this initial foundation. Future integrations must use stable APIs or an
isolated compatibility layer—never copied internals or fragile private hooks.

## Contributing

Open an issue before a substantial change, keep pull requests focused, and run
`./gradlew clean validateAll` before submitting code.

## License

Lumina Engine is licensed under the [GNU General Public License v3.0](LICENSE).

Minecraft is a trademark of Microsoft Corporation. This project is not
affiliated with or endorsed by Microsoft or Mojang Studios.
