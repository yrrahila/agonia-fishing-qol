package io.github.yrrahila.agoniafishingqol;

import java.util.List;
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
    private static float previousBiteScaleProgress;
    private static float biteScaleProgress;
    private static List<TrailSegment> trailSegments = List.of();

    private FishingVisualState() {
    }

    public static void publish(
        FishingHook hook,
        boolean fishApproaching,
        boolean biteReady,
        @Nullable Vec3 visualAnchor,
        float previousWeight,
        float currentWeight,
        float previousScaleProgress,
        float currentScaleProgress,
        List<TrailSegment> visibleTrailSegments
    ) {
        ownHookId = hook.getId();
        approaching = fishApproaching;
        biting = biteReady;
        anchor = visualAnchor;
        previousAnchorWeight = previousWeight;
        anchorWeight = currentWeight;
        previousBiteScaleProgress = previousScaleProgress;
        biteScaleProgress = currentScaleProgress;
        trailSegments = List.copyOf(visibleTrailSegments);
    }

    public static void clear() {
        ownHookId = -1;
        approaching = false;
        biting = false;
        anchor = null;
        previousAnchorWeight = 0.0F;
        anchorWeight = 0.0F;
        previousBiteScaleProgress = 0.0F;
        biteScaleProgress = 0.0F;
        trailSegments = List.of();
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

    public static float interpolatedBiteScaleProgress(float partialTicks) {
        return previousBiteScaleProgress
            + (biteScaleProgress - previousBiteScaleProgress) * partialTicks;
    }

    public static List<TrailSpan> interpolatedTrailSpans(float partialTicks) {
        return trailSegments.stream()
            .map(segment -> {
                Vec3 motion = segment.position().subtract(segment.previousPosition());
                Vec3 end = segment.previousPosition().lerp(segment.position(), partialTicks);
                return new TrailSpan(end.subtract(motion.scale(0.65)), end);
            })
            .toList();
    }

    public record TrailSegment(Vec3 previousPosition, Vec3 position) {
    }

    public record TrailSpan(Vec3 start, Vec3 end) {
    }
}
