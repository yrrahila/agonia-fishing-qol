package io.github.yrrahila.agoniafishingqol;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/** Shared first-person rod transform and its most recently rendered model-tip position. */
public final class FirstPersonRodVisuals {
    public static final float SCALE = 0.78F;
    public static final float UPWARD_OFFSET = 0.055F;
    public static final float CAMERA_OFFSET = -0.38F;
    // The fishing-rod texture reaches its upper tip near this stable model-space point.
    // Capturing one fixed point avoids snapping between bounding-box corners as the hand moves.
    private static final float MODEL_TIP_X = 0.88F;
    private static final float MODEL_TIP_Y = 0.96F;
    private static final float MODEL_TIP_Z = 0.53125F;

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
     * Captures one stable point at the rod's texture tip after Minecraft's real item display
     * transform. The pose contains the interpolated hand animation plus this mod's transform.
     */
    public static void captureRenderedTip(PoseStack.Pose renderedPose) {
        Minecraft client = Minecraft.getInstance();
        Camera camera = client.gameRenderer.mainCamera();
        Quaternionf worldToCamera = camera.rotation().conjugate(new Quaternionf());
        Vector3f cameraLocal = new Vector3f(MODEL_TIP_X, MODEL_TIP_Y, MODEL_TIP_Z)
            .mulPosition(renderedPose.pose())
            .rotate(worldToCamera);
        int levelIdentity = client.level == null ? 0 : System.identityHashCode(client.level);
        latestTip = new TipCapture(new Vec3(cameraLocal.x, cameraLocal.y, cameraLocal.z), levelIdentity);
    }

    public static @Nullable Vec3 currentWorldTip() {
        Minecraft client = Minecraft.getInstance();
        TipCapture capture = latestTip;
        if (capture == null
            || client.level == null
            || capture.levelIdentity() != System.identityHashCode(client.level)) {
            return null;
        }

        Camera camera = client.gameRenderer.mainCamera();
        Vec3 cameraLocal = capture.cameraLocal();
        Vector3f worldOffset = new Vector3f((float)cameraLocal.x, (float)cameraLocal.y, (float)cameraLocal.z)
            .rotate(camera.rotation());
        return camera.position().add(worldOffset.x, worldOffset.y, worldOffset.z);
    }

    public static void clear() {
        latestTip = null;
    }

    private record TipCapture(Vec3 cameraLocal, int levelIdentity) {
    }
}
