package io.github.yrrahila.agoniafishingqol;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

/** Shared first-person rod transform and its most recently rendered model-tip position. */
public final class FirstPersonRodVisuals {
    public static final float SCALE = 0.78F;
    public static final float UPWARD_OFFSET = 0.055F;
    public static final float CAMERA_OFFSET = -0.38F;
    private static final long CAPTURE_MAX_AGE_NANOS = 250_000_000L;

    private static boolean captureActive;
    private static @Nullable TipCapture latestTip;

    private FirstPersonRodVisuals() {
    }

    public static void beginCapture(PoseStack poseStack) {
        captureActive = true;
        poseStack.pushPose();
        poseStack.translate(0.0F, UPWARD_OFFSET, CAMERA_OFFSET);
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    public static void endCapture(PoseStack poseStack) {
        poseStack.popPose();
        captureActive = false;
    }

    public static boolean isCaptureActive() {
        return captureActive;
    }

    /**
     * Finds the highest projected extent after Minecraft's real item display transform has
     * been applied. The surrounding pose already contains the hand animation and this mod's
     * scale/translation, so the captured point remains attached when those transforms change.
     */
    public static void captureRenderedTip(ItemStackRenderState renderState, PoseStack poseStack) {
        Minecraft client = Minecraft.getInstance();
        Camera camera = client.gameRenderer.mainCamera();
        Matrix4f renderedPose = new Matrix4f(poseStack.last().pose());
        Quaternionf worldToCamera = camera.rotation().conjugate(new Quaternionf());
        TipCandidate[] best = {null};

        renderState.visitExtents(extent -> {
            Vector3f cameraLocal = agoniaFishingQol$toCameraLocal(extent, renderedPose, worldToCamera);
            float depth = Math.max(0.05F, -cameraLocal.z);
            float projectedHeight = cameraLocal.y / depth;
            if (best[0] == null || projectedHeight > best[0].projectedHeight()) {
                best[0] = new TipCandidate(cameraLocal, projectedHeight);
            }
        });

        if (best[0] != null) {
            Vector3f point = best[0].cameraLocal();
            latestTip = new TipCapture(new Vec3(point.x, point.y, point.z), System.nanoTime());
        }
    }

    public static @Nullable Vec3 currentWorldTip() {
        TipCapture capture = latestTip;
        if (capture == null || System.nanoTime() - capture.capturedAtNanos() > CAPTURE_MAX_AGE_NANOS) {
            return null;
        }

        Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
        Vec3 cameraLocal = capture.cameraLocal();
        Vector3f worldOffset = new Vector3f((float)cameraLocal.x, (float)cameraLocal.y, (float)cameraLocal.z)
            .rotate(camera.rotation());
        return camera.position().add(worldOffset.x, worldOffset.y, worldOffset.z);
    }

    private static Vector3f agoniaFishingQol$toCameraLocal(
        Vector3fc modelExtent,
        Matrix4f renderedPose,
        Quaternionf worldToCamera
    ) {
        return new Vector3f(modelExtent).mulPosition(renderedPose).rotate(worldToCamera);
    }

    private record TipCandidate(Vector3f cameraLocal, float projectedHeight) {
    }

    private record TipCapture(Vec3 cameraLocal, long capturedAtNanos) {
    }
}
