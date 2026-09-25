package io.github.yrrahila.agoniafishingqol.mixin.client;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import io.github.yrrahila.agoniafishingqol.StaticWaterAnimation;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpriteContents.class)
public abstract class SpriteContentsMixin {
    @Shadow
    @Final
    private Identifier name;

    @Inject(method = "createAnimationState", at = @At("RETURN"))
    private void agoniaFishingQol$registerStaticWaterAnimation(
        GpuBufferSlice uboSlice,
        int spriteUboSize,
        CallbackInfoReturnable<SpriteContents.AnimationState> cir
    ) {
        SpriteContents.AnimationState state = cir.getReturnValue();
        if (state != null && StaticWaterAnimation.isWaterSprite(this.name)) {
            StaticWaterAnimation.freeze(state);
        }
    }
}
