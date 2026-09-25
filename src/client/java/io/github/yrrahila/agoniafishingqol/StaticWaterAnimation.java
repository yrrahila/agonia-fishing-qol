package io.github.yrrahila.agoniafishingqol;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.resources.Identifier;

/** Tracks only the two vanilla water sprite animation states so their frames stay fixed. */
public final class StaticWaterAnimation {
    private static final Identifier WATER_STILL = Identifier.withDefaultNamespace("block/water_still");
    private static final Identifier WATER_FLOW = Identifier.withDefaultNamespace("block/water_flow");
    private static final Map<SpriteContents.AnimationState, Boolean> FROZEN_STATES =
        Collections.synchronizedMap(new WeakHashMap<>());

    private StaticWaterAnimation() {
    }

    public static boolean isWaterSprite(Identifier sprite) {
        return WATER_STILL.equals(sprite) || WATER_FLOW.equals(sprite);
    }

    public static synchronized void freeze(SpriteContents.AnimationState state) {
        FROZEN_STATES.put(state, false);
    }

    public static synchronized boolean shouldFreezeTick(SpriteContents.AnimationState state) {
        Boolean initialized = FROZEN_STATES.get(state);
        if (initialized == null) {
            return false;
        }
        if (!initialized) {
            FROZEN_STATES.put(state, true);
            return false;
        }
        return true;
    }
}
