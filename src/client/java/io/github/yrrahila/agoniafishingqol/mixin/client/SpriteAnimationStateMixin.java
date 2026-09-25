package io.github.yrrahila.agoniafishingqol.mixin.client;

import io.github.yrrahila.agoniafishingqol.StaticWaterAnimation;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteContents.AnimationState.class)
public abstract class SpriteAnimationStateMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void agoniaFishingQol$freezeWaterFrame(CallbackInfo ci) {
        if (StaticWaterAnimation.shouldFreezeTick((SpriteContents.AnimationState)(Object)this)) {
            ci.cancel();
        }
    }
}
