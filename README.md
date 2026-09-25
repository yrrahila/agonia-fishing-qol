# Agonia Fishing QoL

A client-side Fabric mod that makes manual fishing clearer and easier to follow on Agonia SMP.

## Features

- **Fishing states** — Red while waiting, yellow while a fish approaches, and green when a bite is ready.
- **Bite alerts** — Large visual feedback and a repeating, selectable notification sound.
- **Estimated timing** — Shows an honest bite-time range adjusted for Lure, rain, and sky exposure.
- **Compact HUD** — Displays cast time, estimated bite time, and fishing-rod durability.
- **Durability warning** — Warns when the held fishing rod is close to breaking.
- **Local bobber visuals** — Adds a clearer 3D bobber for your own cast without changing other players' bobbers.
- **Improved fishing line** — Uses a thicker, color-coded line aligned with the first-person rod tip.
- **Approach trail** — Replaces your own fishing wake with a smooth yellow or green trail.
- **Clear water** — Makes the bobber and fishing activity easier to see below the surface.
- **Sound settings** — Includes 21 vanilla sound presets and a 0–200% alert-volume slider.
- **Master switch** — Disables every mod effect and restores normal client rendering at runtime.
- **Multiplayer-safe** — Requires no server mod, sends no custom packets, and tracks only your own bobber.
- **No automation** — Casting, reeling, movement, inventory actions, and rod switching remain completely manual.

## Compatibility

- Minecraft `26.2`
- Fabric Loader `0.19.3` or newer
- Fabric API `0.156.0+26.2` or newer compatible 26.2 build
- Java `25`
- Mod Menu `20.0.3` is optional and provides access to settings

## Download

Players should download the `.jar` file from the repository's [Releases page](https://github.com/yrrahila/agonia-fishing-qol/releases) and place it in the Modrinth instance's `mods` folder.

Do not use **Code → Download ZIP** to install the mod. That ZIP is GitHub's automatic source-code archive for developers, so it correctly contains Gradle files, source code, and build scripts.

## Build from source

```shell
./gradlew build
```

The compiled mod is written to `build/libs/`.

## License

MIT
