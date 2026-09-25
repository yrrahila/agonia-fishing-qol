package io.github.yrrahila.agoniafishingqol.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.yrrahila.agoniafishingqol.FirstPersonRodVisuals;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Inject(method = "renderItem", at = @At("HEAD"))
    private void agoniaFishingQol$pushSmallerFishingRod(
        LivingEntity entity,
        ItemStack itemStack,
        ItemDisplayContext displayContext,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        int lightCoords,
        CallbackInfo ci
    ) {
        if (agoniaFishingQol$isFirstPersonFishingRod(itemStack, displayContext)) {
            FirstPersonRodVisuals.beginCapture(poseStack);
        }
    }

    @Redirect(
        method = "renderItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"
        )
    )
    private void agoniaFishingQol$captureTransformedRodTip(
        ItemStackRenderState renderState,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        int lightCoords,
        int overlayCoords,
        int outlineColor
    ) {
        if (FirstPersonRodVisuals.isCaptureActive()) {
            FirstPersonRodVisuals.captureRenderedTip(renderState, poseStack);
        }
        renderState.submit(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor);
    }

    @Inject(method = "renderItem", at = @At("RETURN"))
    private void agoniaFishingQol$popSmallerFishingRod(
        LivingEntity entity,
        ItemStack itemStack,
        ItemDisplayContext displayContext,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        int lightCoords,
        CallbackInfo ci
    ) {
        if (agoniaFishingQol$isFirstPersonFishingRod(itemStack, displayContext)) {
            FirstPersonRodVisuals.endCapture(poseStack);
        }
    }

    private static boolean agoniaFishingQol$isFirstPersonFishingRod(
        ItemStack itemStack,
        ItemDisplayContext displayContext
    ) {
        return itemStack.is(Items.FISHING_ROD)
            && (displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
    }
}
