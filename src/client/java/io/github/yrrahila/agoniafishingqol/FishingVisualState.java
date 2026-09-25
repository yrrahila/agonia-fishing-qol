package io.github.yrrahila.agoniafishingqol;

import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Immutable-at-render-time visual data published by the client tick tracker. */
public final class FishingVisualState {
    private static int ownHookId = -1;
    private static boolean approaching;
    private static boolean biting;
    private static @Nullable Vec3 anchor;
    private static float previousAnchorWeight;
    private static float anchorWeight;

    private FishingVisualState() {
    }

    public static void publish(
        FishingHook hook,
        boolean fishApproaching,
        boolean biteReady,
        @Nullable Vec3 visualAnchor,
        float previousWeight,
        float currentWeight
    ) {
        ownHookId = hook.getId();
        approaching = fishApproaching;
        biting = biteReady;
        anchor = visualAnchor;
        previousAnchorWeight = previousWeight;
        anchorWeight = currentWeight;
    }

    public static void clear() {
        ownHookId = -1;
        approaching = false;
        biting = false;
        anchor = null;
        previousAnchorWeight = 0.0F;
        anchorWeight = 0.0F;
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

    public static @Nullable Vec3 anchor() {
        return anchor;
    }

    public static float interpolatedAnchorWeight(float partialTicks) {
        return previousAnchorWeight + (anchorWeight - previousAnchorWeight) * partialTicks;
    }
}
