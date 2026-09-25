package io.github.yrrahila.agoniafishingqol.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.yrrahila.agoniafishingqol.AgoniaFishingQolClient;
import io.github.yrrahila.agoniafishingqol.FirstPersonRodVisuals;
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
    private static final int WAITING_LINE_COLOR = 0xFFFF3030;
    private static final int APPROACHING_LINE_COLOR = 0xFFFFFF00;
    private static final int BITE_LINE_COLOR = 0xFF00FF00;
    private static final int WAITING_BOBBER_COLOR = 0xFFD62828;
    private static final int APPROACHING_BOBBER_COLOR = 0xFFE6D000;
    private static final int BITE_BOBBER_COLOR = 0xFF00D628;
    private static final float NORMAL_BOBBER_SCALE = 0.58F;
    private static final float BITE_BOBBER_SCALE = 1.35F;
    private static final float LINE_WIDTH_MULTIPLIER = 2.0F;
    private static final float TRAIL_HALF_SIZE = 0.055F;
    private static final int LINE_SEGMENTS = 24;

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
        if (!AgoniaFishingQolClient.isEnabled()) {
            extension.agoniaFishingQol$setOwnHook(false);
            extension.agoniaFishingQol$setApproaching(false);
            extension.agoniaFishingQol$setBiting(false);
            extension.agoniaFishingQol$setBiteScaleProgress(0.0F);
            extension.agoniaFishingQol$setTrailSpans(java.util.List.of());
            return;
        }

        boolean ownHook = FishingVisualState.isOwnHook(entity);
        boolean approaching = ownHook && FishingVisualState.isApproaching();
        boolean biting = ownHook && FishingVisualState.isBiting();
        extension.agoniaFishingQol$setOwnHook(ownHook);
        extension.agoniaFishingQol$setApproaching(approaching);
        extension.agoniaFishingQol$setBiting(biting);
        extension.agoniaFishingQol$setBiteScaleProgress(
            ownHook ? FishingVisualState.interpolatedBiteScaleProgress(partialTicks) : 0.0F
        );
        extension.agoniaFishingQol$setTrailSpans(
            ownHook ? FishingVisualState.interpolatedTrailSpans(partialTicks) : java.util.List.of()
        );

        Minecraft client = Minecraft.getInstance();
        Vec3 firstPersonRodTip = ownHook && client.options.getCameraType().isFirstPerson()
            ? FirstPersonRodVisuals.currentWorldTip()
            : null;
        if (firstPersonRodTip != null) {
            // Use the renderer's already-interpolated hook coordinates so both line endpoints
            // are sampled in the same frame and coordinate space.
            Vec3 actualHookPosition = new Vec3(state.x, state.y + 0.25, state.z);
            state.lineOriginOffset = firstPersonRodTip.subtract(actualHookPosition);
        }

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
        if (!AgoniaFishingQolClient.isEnabled() || !extension.agoniaFishingQol$isOwnHook()) {
            return;
        }

        boolean approaching = extension.agoniaFishingQol$isApproaching();
        boolean biting = extension.agoniaFishingQol$isBiting();
        int lineColor = biting ? BITE_LINE_COLOR : approaching ? APPROACHING_LINE_COLOR : WAITING_LINE_COLOR;
        int bobberColor = biting ? BITE_BOBBER_COLOR : approaching
            ? APPROACHING_BOBBER_COLOR
            : WAITING_BOBBER_COLOR;
        float bobberScale = Mth.lerp(
            extension.agoniaFishingQol$biteScaleProgress(),
            NORMAL_BOBBER_SCALE,
            BITE_BOBBER_SCALE
        );

        poseStack.pushPose();
        poseStack.pushPose();
        poseStack.scale(bobberScale, bobberScale, bobberScale);
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.debugQuads(), (pose, buffer) -> {
            // A compact two-part 3D float. The color-only render type is unlit and keeps all
            // transformations local to this custom geometry submission.
            agoniaFishingQol$cuboid(buffer, pose, -0.22F, -0.38F, -0.22F, 0.22F, 0.06F, 0.22F, bobberColor);
            agoniaFishingQol$cuboid(buffer, pose, -0.07F, 0.06F, -0.07F, 0.07F, 0.34F, 0.07F, bobberColor);
        });
        poseStack.popPose();

        if (!extension.agoniaFishingQol$trailSpans().isEmpty()) {
            submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.debugQuads(), (pose, buffer) -> {
                for (FishingVisualState.TrailSpan span : extension.agoniaFishingQol$trailSpans()) {
                    float startX = (float)(span.start().x - state.x);
                    float startY = (float)(span.start().y - state.y);
                    float startZ = (float)(span.start().z - state.z);
                    float endX = (float)(span.end().x - state.x);
                    float endY = (float)(span.end().y - state.y);
                    float endZ = (float)(span.end().z - state.z);
                    agoniaFishingQol$cuboid(
                        buffer,
                        pose,
                        Math.min(startX, endX) - TRAIL_HALF_SIZE,
                        Math.min(startY, endY) - TRAIL_HALF_SIZE * 0.35F,
                        Math.min(startZ, endZ) - TRAIL_HALF_SIZE,
                        Math.max(startX, endX) + TRAIL_HALF_SIZE,
                        Math.max(startY, endY) + TRAIL_HALF_SIZE * 0.35F,
                        Math.max(startZ, endZ) + TRAIL_HALF_SIZE,
                        lineColor
                    );
                }
            });
        }

        float xa = (float)state.lineOriginOffset.x;
        float ya = (float)state.lineOriginOffset.y;
        float za = (float)state.lineOriginOffset.z;
        float width = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth
            * LINE_WIDTH_MULTIPLIER;
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) -> {
            for (int i = 0; i < LINE_SEGMENTS; i++) {
                float a0 = (float)i / LINE_SEGMENTS;
                float a1 = (float)(i + 1) / LINE_SEGMENTS;
                agoniaFishingQol$stringVertex(xa, ya, za, buffer, pose, a0, a1, width, lineColor);
                agoniaFishingQol$stringVertex(xa, ya, za, buffer, pose, a1, a0, width, lineColor);
            }
        });
        poseStack.popPose();
        ci.cancel();
    }

    private static void agoniaFishingQol$cuboid(
        VertexConsumer builder,
        PoseStack.Pose pose,
        float minX,
        float minY,
        float minZ,
        float maxX,
        float maxY,
        float maxZ,
        int color
    ) {
        agoniaFishingQol$quad(builder, pose, minX, minY, minZ, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, color);
        agoniaFishingQol$quad(builder, pose, maxX, minY, maxZ, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, color);
        agoniaFishingQol$quad(builder, pose, minX, minY, maxZ, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, color);
        agoniaFishingQol$quad(builder, pose, maxX, minY, minZ, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, color);
        agoniaFishingQol$quad(builder, pose, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ, color);
        agoniaFishingQol$quad(builder, pose, minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, minX, minY, minZ, color);
    }

    private static void agoniaFishingQol$quad(
        VertexConsumer builder,
        PoseStack.Pose pose,
        float x0,
        float y0,
        float z0,
        float x1,
        float y1,
        float z1,
        float x2,
        float y2,
        float z2,
        float x3,
        float y3,
        float z3,
        int color
    ) {
        builder.addVertex(pose, x0, y0, z0).setColor(color);
        builder.addVertex(pose, x1, y1, z1).setColor(color);
        builder.addVertex(pose, x2, y2, z2).setColor(color);
        builder.addVertex(pose, x3, y3, z3).setColor(color);
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
