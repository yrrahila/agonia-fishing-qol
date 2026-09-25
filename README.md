# Agonia Fishing QoL

A strictly client-side Fabric mod for Minecraft 26.2 that makes manual fishing easier to read without automating any action.

Author: Yxad

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
- A large yellow `INCOMING...` alert and short note-block sound when the local hook's real server-sent fish trail begins.
- A larger green `BITE!` alert, a 35% larger bite-state bobber, and a lower-pitched challenge-complete sound played once when the bite becomes ready.
- A compact middle-right HUD, visible only while holding a fishing rod, with a bold `Agonia Fishing QoL` title and separate `Cast`, `Estimated`, and `Durability` rows.
- A static numerical bite-time range based on vanilla's random wait bounds, Lure, rain, and sky exposure. It changes to `Incoming` only for the verified approach trail and `Ready` only for the synchronized bite state.
- A client-only glow and matching solid red (waiting), yellow (approaching), or green (bite-ready) bobber and clearly thicker line.
- A render-only stable waiting anchor that blends in and out over eight ticks, including between-frame interpolation, while fishing logic continues to use the real entity position.
- The verified local-player `FISHING` approach wake is replaced client-side with a managed yellow trail. Every still-visible segment is recolored green as soon as the bite state begins, while other players' fishing particles are left unchanged.
- All ordinary `BUBBLE` particles are hidden client-side, and local `SPLASH`, `UNDERWATER`, and `BUBBLE_POP` particles within four blocks of the player's bobber are removed. Bubble-column mechanics and other distinct world particles are unchanged.
- Uniform translucent still/flow water textures produce a clearer, smooth, biome-tinted surface without shader, framebuffer, fog, physics, movement, or server-state changes.

## Multiplayer safety

The tracker reads only `LocalPlayer.fishing` and also verifies that the hook's owner is the local player. Bite state comes directly from that synchronized hook. Approach particles are accepted only when their exact vanilla fishing-trail packet signature is closest to that hook; ambiguous overlapping trails are ignored. The mod contains no casting, reeling, inventory, movement, interaction, or packet-sending code.

The exact server-side random fishing countdown and the server's internal approaching-fish phase are not synchronized to clients. For that reason, the HUD reports a range rather than a guaranteed countdown and does not claim to identify the approaching phase. Modified server fishing mechanics may make the estimate less accurate.
