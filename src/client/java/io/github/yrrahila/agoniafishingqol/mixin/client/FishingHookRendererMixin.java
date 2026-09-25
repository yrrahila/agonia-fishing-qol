package io.github.yrrahila.agoniafishingqol.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.yrrahila.agoniafishingqol.FishingHookRenderStateAccess;
import io.github.yrrahila.agoniafishingqol.FishingVisualState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHookRenderer.class)
public abstract class FishingHookRendererMixin {
    private static final int WAITING_COLOR = 0xFFFF3030;
    private static final int APPROACHING_COLOR = 0xFFFFFF00;
    private static final int BITE_COLOR = 0xFF00FF00;
    private static final float NORMAL_BOBBER_SCALE = 0.58F;
    private static final float BITE_BOBBER_SCALE = 1.35F;
    private static final float LINE_WIDTH_MULTIPLIER = 2.0F;

    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/projectile/FishingHook;Lnet/minecraft/client/renderer/entity/state/FishingHookRenderState;F)V",
        at = @At("TAIL")
    )
    private void agoniaFishingQol$extractOwnHookVisuals(
        FishingHook entity,
        FishingHookRenderState state,
        float partialTicks,
        CallbackInfo ci
    ) {
        FishingHookRenderStateAccess extension = (FishingHookRenderStateAccess)state;
        boolean ownHook = FishingVisualState.isOwnHook(entity);
        boolean approaching = ownHook && FishingVisualState.isApproaching();
        boolean biting = ownHook && FishingVisualState.isBiting();
        extension.agoniaFishingQol$setOwnHook(ownHook);
        extension.agoniaFishingQol$setApproaching(approaching);
        extension.agoniaFishingQol$setBiting(biting);

        Vec3 anchor = ownHook ? FishingVisualState.anchor() : null;
        float anchorWeight = anchor == null ? 0.0F : FishingVisualState.interpolatedAnchorWeight(partialTicks);
        if (anchorWeight > 0.0F) {
            double actualX = state.x;
            double actualY = state.y;
            double actualZ = state.z;
            double renderedX = Mth.lerp(anchorWeight, actualX, anchor.x);
            double renderedY = Mth.lerp(anchorWeight, actualY, anchor.y);
            double renderedZ = Mth.lerp(anchorWeight, actualZ, anchor.z);
            state.lineOriginOffset = state.lineOriginOffset.add(
                actualX - renderedX,
                actualY - renderedY,
                actualZ - renderedZ
            );
            state.x = renderedX;
            state.y = renderedY;
            state.z = renderedZ;
        }
    }

    @Inject(
        method = "submit(Lnet/minecraft/client/renderer/entity/state/FishingHookRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void agoniaFishingQol$renderOwnHook(
        FishingHookRenderState state,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        CameraRenderState camera,
        CallbackInfo ci
    ) {
        FishingHookRenderStateAccess extension = (FishingHookRenderStateAccess)state;
        if (!extension.agoniaFishingQol$isOwnHook()) {
            return;
        }

        boolean approaching = extension.agoniaFishingQol$isApproaching();
        boolean biting = extension.agoniaFishingQol$isBiting();
        int phaseColor = biting ? BITE_COLOR : approaching ? APPROACHING_COLOR : WAITING_COLOR;
        float bobberScale = biting ? BITE_BOBBER_SCALE : NORMAL_BOBBER_SCALE;

        poseStack.pushPose();
        poseStack.pushPose();
        poseStack.scale(bobberScale, bobberScale, bobberScale);
        poseStack.mulPose(camera.orientation);
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.debugQuads(), (pose, buffer) -> {
            // Rebuild the vanilla 8x8 bobber silhouette with untextured color-only quads. Unlike
            // texture tinting, every visible pixel receives the state color instead of retaining red texels.
            agoniaFishingQol$bobberRect(buffer, pose, -0.125F, 0.0F, 0.25F, 0.375F, phaseColor);
            agoniaFishingQol$bobberRect(buffer, pose, 0.0F, -0.25F, 0.125F, 0.0F, phaseColor);
            agoniaFishingQol$bobberRect(buffer, pose, -0.25F, -0.375F, -0.125F, -0.25F, phaseColor);
            agoniaFishingQol$bobberRect(buffer, pose, 0.0F, -0.5F, 0.125F, -0.25F, phaseColor);
            agoniaFishingQol$bobberRect(buffer, pose, -0.125F, -0.5F, 0.0F, -0.375F, phaseColor);
        });
        poseStack.popPose();

        float xa = (float)state.lineOriginOffset.x;
        float ya = (float)state.lineOriginOffset.y;
        float za = (float)state.lineOriginOffset.z;
        float width = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth
            * LINE_WIDTH_MULTIPLIER;
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) -> {
            for (int i = 0; i < 16; i++) {
                float a0 = (float)i / 16.0F;
                float a1 = (float)(i + 1) / 16.0F;
                agoniaFishingQol$stringVertex(xa, ya, za, buffer, pose, a0, a1, width, phaseColor);
                agoniaFishingQol$stringVertex(xa, ya, za, buffer, pose, a1, a0, width, phaseColor);
            }
        });
        poseStack.popPose();
        ci.cancel();
    }

    private static void agoniaFishingQol$bobberRect(
        VertexConsumer builder,
        PoseStack.Pose pose,
        float left,
        float bottom,
        float right,
        float top,
        int color
    ) {
        builder.addVertex(pose, left, bottom, 0.0F).setColor(color);
        builder.addVertex(pose, right, bottom, 0.0F).setColor(color);
        builder.addVertex(pose, right, top, 0.0F).setColor(color);
        builder.addVertex(pose, left, top, 0.0F).setColor(color);
    }

    private static void agoniaFishingQol$stringVertex(
        float xa,
        float ya,
        float za,
        VertexConsumer buffer,
        PoseStack.Pose pose,
        float a,
        float nextA,
        float width,
        int color
    ) {
        float x = xa * a;
        float y = ya * (a * a + a) * 0.5F + 0.25F;
        float z = za * a;
        float nx = xa * nextA - x;
        float ny = ya * (nextA * nextA + nextA) * 0.5F + 0.25F - y;
        float nz = za * nextA - z;
        float length = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        nx /= length;
        ny /= length;
        nz /= length;
        buffer.addVertex(pose, x, y, z).setColor(color).setNormal(pose, nx, ny, nz).setLineWidth(width);
    }
}
