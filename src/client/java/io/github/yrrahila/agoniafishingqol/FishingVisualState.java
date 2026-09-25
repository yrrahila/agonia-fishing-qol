package io.github.yrrahila.agoniafishingqol;

import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Immutable-at-render-time visual data published by the client tick tracker. */
public final class FishingVisualState {
    private static int ownHookId = -1;
    private static boolean biting;
    private static @Nullable Vec3 frozenPosition;

    private FishingVisualState() {
    }

    public static void publish(FishingHook hook, boolean biteReady, @Nullable Vec3 renderPosition) {
        ownHookId = hook.getId();
        biting = biteReady;
        frozenPosition = renderPosition;
    }

    public static void clear() {
        ownHookId = -1;
        biting = false;
        frozenPosition = null;
    }

    public static boolean isOwnHook(FishingHook hook) {
        return hook.getId() == ownHookId;
    }

    public static boolean isBiting() {
        return biting;
    }

    public static @Nullable Vec3 frozenPosition() {
        return frozenPosition;
    }
}
