# Agonia Fishing QoL

A strictly client-side Fabric mod for Minecraft 26.2 that makes manual fishing easier to read without automating any action.

## Principles

- Every cast, reel, item switch, movement, and interaction remains manual.
- The mod sends no custom packets and requires nothing on the server.
- All indicators are limited to the local player's own fishing hook.
- Bite timing is labelled as an estimate because the server's random countdown is not exposed to clients.

## Requirements

- Minecraft Java Edition 26.2
- Fabric Loader 0.19.3 or newer
- Fabric API 0.156.0+26.2 or newer compatible 26.2 build
- Java 25

## Build

```shell
./gradlew build
```

The distributable JAR is written to `build/libs/`.

## Included QoL features

- One action-bar warning when a held fishing rod reaches 10 durability or less.
- A yellow `WAIT...` alert and distinct approach sound when the local hook's real server-sent fish trail begins.
- A substantially enlarged green `BITE!` alert with a lower-pitched bell pulse that repeats only while the bite remains ready.
- A compact status HUD showing `Not Cast`, `Waiting`, `Fish Approaching`, or `Bite Ready`.
- A deliberately labelled estimated bite-time range based on vanilla's random wait bounds, Lure, rain, and sky exposure.
- A client-only glow, a red waiting line, a green bite-ready line, and an enlarged gold bite-ready bobber.
- A render-only resting anchor that removes the local bobber's idle bobbing after it settles without moving the real entity.
- A denser, larger version of the vanilla fishing wake for the local player's approach trail, with idle fishing splashes suppressed.
- Static vanilla water sprite frames, with no changes to water blocks, physics, movement, or server state.

## Multiplayer safety

The tracker reads only `LocalPlayer.fishing` and also verifies that the hook's owner is the local player. Bite state comes directly from that synchronized hook. Approach particles are accepted only when their exact vanilla fishing-trail packet signature is closest to that hook; ambiguous overlapping trails are ignored. The mod contains no casting, reeling, inventory, movement, interaction, or packet-sending code.

The exact server-side random fishing countdown and the server's internal approaching-fish phase are not synchronized to clients. For that reason, the HUD reports a range rather than a guaranteed countdown and does not claim to identify the approaching phase. Modified server fishing mechanics may make the estimate less accurate.
