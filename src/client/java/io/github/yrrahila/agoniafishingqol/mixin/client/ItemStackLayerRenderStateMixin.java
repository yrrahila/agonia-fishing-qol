package io.github.yrrahila.agoniafishingqol.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.yrrahila.agoniafishingqol.AgoniaFishingQolClient;
import io.github.yrrahila.agoniafishingqol.FirstPersonRodVisuals;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public abstract class ItemStackLayerRenderStateMixin {
    @Inject(
        method = "submit",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState$LayerRenderState;applyTransform(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;)V",
            shift = At.Shift.AFTER
        )
    )
    private void agoniaFishingQol$captureRodTipAfterItemTransform(
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        int lightCoords,
        int overlayCoords,
        int outlineColor,
        CallbackInfo ci
    ) {
        if (AgoniaFishingQolClient.isEnabled() && FirstPersonRodVisuals.isCaptureActive()) {
            FirstPersonRodVisuals.captureRenderedTip(poseStack.last());
        }
    }
}
