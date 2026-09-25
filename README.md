# Agonia Fishing QoL

A strictly client-side Fabric mod for Minecraft 26.2 that makes manual fishing easier to read without automating any action.

## Principles

- Every cast, reel, item switch, movement, and interaction remains manual.
- The mod sends no custom packets and requires nothing on the server.
- All indicators are limited to the local player's own fishing hook.
- Bite timing is labelled as an estimate because the server's random countdown is not exposed to clients.

## Requirements

- Minecraft Java Edition 26.2
- Fabric Loader 0.19.5 or newer
- Fabric API 0.161.0+26.2 or newer compatible 26.2 build
- Java 25

## Build

```shell
./gradlew build
```

The distributable JAR is written to `build/libs/`.

## Included QoL features

- One action-bar warning when a held fishing rod reaches 10 durability or less.
- A built-in pling sound and `BITE!` HUD notification for a confirmed bite.
- A compact status HUD showing `Not Cast`, `Waiting`, or `Bite Ready`.
- A deliberately labelled estimated bite-time range based on vanilla's random wait bounds, Lure, rain, and sky exposure.
- A client-only glow and one subtle particle at the local player's bobber every 16 ticks.
- Vanilla-compatible open-water evaluation over the same 5×5×4 area, with a short failure explanation.

## Multiplayer safety

The tracker reads only `LocalPlayer.fishing` and also verifies that the hook's owner is the local player. It never scans nearby hooks for bite events. The mod contains no casting, reeling, inventory, movement, interaction, or packet-sending code.

The exact server-side random fishing countdown and the server's internal approaching-fish phase are not synchronized to clients. For that reason, the HUD reports a range rather than a guaranteed countdown and does not claim to identify the approaching phase. Modified server fishing mechanics may make the estimate less accurate.
