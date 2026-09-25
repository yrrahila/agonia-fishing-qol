package io.github.yrrahila.agoniafishingqol.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.yrrahila.agoniafishingqol.FishingHookRenderStateAccess;
import io.github.yrrahila.agoniafishingqol.FishingVisualState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHookRenderer.class)
public abstract class FishingHookRendererMixin {
    private static final RenderType AGONIA_FISHING_QOL_RENDER_TYPE = RenderTypes.entityCutoutCull(
        Identifier.withDefaultNamespace("textures/entity/fishing/fishing_hook.png")
    );
    private static final int WAITING_LINE_COLOR = 0xFFFF3030;
    private static final int BITE_LINE_COLOR = 0xFF30FF60;
    private static final int BITE_BOBBER_COLOR = 0xFFFFC400;

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
        boolean biting = ownHook && FishingVisualState.isBiting();
        extension.agoniaFishingQol$setOwnHook(ownHook);
        extension.agoniaFishingQol$setBiting(biting);

        Vec3 frozenPosition = ownHook && !biting ? FishingVisualState.frozenPosition() : null;
        if (frozenPosition != null) {
            double actualX = state.x;
            double actualY = state.y;
            double actualZ = state.z;
            state.lineOriginOffset = state.lineOriginOffset.add(
                actualX - frozenPosition.x,
                actualY - frozenPosition.y,
                actualZ - frozenPosition.z
            );
            state.x = frozenPosition.x;
            state.y = frozenPosition.y;
            state.z = frozenPosition.z;
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

        boolean biting = extension.agoniaFishingQol$isBiting();
        int bobberColor = biting ? BITE_BOBBER_COLOR : 0xFFFFFFFF;
        int lineColor = biting ? BITE_LINE_COLOR : WAITING_LINE_COLOR;
        float bobberScale = biting ? 1.0F : 0.5F;

        poseStack.pushPose();
        poseStack.pushPose();
        poseStack.scale(bobberScale, bobberScale, bobberScale);
        poseStack.mulPose(camera.orientation);
        submitNodeCollector.submitCustomGeometry(poseStack, AGONIA_FISHING_QOL_RENDER_TYPE, (pose, buffer) -> {
            agoniaFishingQol$bobberVertex(buffer, pose, state.lightCoords, 0.0F, 0, 0, 1, bobberColor);
            agoniaFishingQol$bobberVertex(buffer, pose, state.lightCoords, 1.0F, 0, 1, 1, bobberColor);
            agoniaFishingQol$bobberVertex(buffer, pose, state.lightCoords, 1.0F, 1, 1, 0, bobberColor);
            agoniaFishingQol$bobberVertex(buffer, pose, state.lightCoords, 0.0F, 1, 0, 0, bobberColor);
        });
        poseStack.popPose();

        float xa = (float)state.lineOriginOffset.x;
        float ya = (float)state.lineOriginOffset.y;
        float za = (float)state.lineOriginOffset.z;
        float width = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth;
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) -> {
            for (int i = 0; i < 16; i++) {
                float a0 = (float)i / 16.0F;
                float a1 = (float)(i + 1) / 16.0F;
                agoniaFishingQol$stringVertex(xa, ya, za, buffer, pose, a0, a1, width, lineColor);
                agoniaFishingQol$stringVertex(xa, ya, za, buffer, pose, a1, a0, width, lineColor);
            }
        });
        poseStack.popPose();
        ci.cancel();
    }

    private static void agoniaFishingQol$bobberVertex(
        VertexConsumer builder,
        PoseStack.Pose pose,
        int lightCoords,
        float x,
        int y,
        int u,
        int v,
        int color
    ) {
        builder.addVertex(pose, x - 0.5F, y - 0.5F, 0.0F)
            .setColor(color)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(lightCoords)
            .setNormal(pose, 0.0F, 1.0F, 0.0F);
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
