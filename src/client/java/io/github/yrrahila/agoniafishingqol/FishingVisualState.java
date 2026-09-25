package io.github.yrrahila.agoniafishingqol;

import net.minecraft.world.entity.projectile.FishingHook;

/** Immutable-at-render-time visual data published by the client tick tracker. */
public final class FishingVisualState {
    private static int ownHookId = -1;
    private static boolean approaching;
    private static boolean biting;

    private FishingVisualState() {
    }

    public static void publish(FishingHook hook, boolean fishApproaching, boolean biteReady) {
        ownHookId = hook.getId();
        approaching = fishApproaching;
        biting = biteReady;
    }

    public static void clear() {
        ownHookId = -1;
        approaching = false;
        biting = false;
    }

    public static boolean isOwnHook(FishingHook hook) {
        return hook.getId() == ownHookId;
    }

    public static boolean isBiting() {
        return biting;
    }

    public static boolean isApproaching() {
        return approaching;
    }
}
