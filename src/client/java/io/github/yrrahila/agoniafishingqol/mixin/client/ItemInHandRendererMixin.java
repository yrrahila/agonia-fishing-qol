package io.github.yrrahila.agoniafishingqol.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.yrrahila.agoniafishingqol.AgoniaFishingQolClient;
import io.github.yrrahila.agoniafishingqol.FirstPersonRodVisuals;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
        return AgoniaFishingQolClient.isEnabled()
            && itemStack.is(Items.FISHING_ROD)
            && (displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
    }
}
