package io.github.yrrahila.agoniafishingqol.mixin.client;

import io.github.yrrahila.agoniafishingqol.FishingTrailParticleAccess;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.LightCoordsUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Particle.class)
public abstract class FishingTrailParticleMixin implements FishingTrailParticleAccess {
    @Unique
    private boolean agoniaFishingQol$fullBright;

    @Override
    public void agoniaFishingQol$setFullBright(boolean fullBright) {
        agoniaFishingQol$fullBright = fullBright;
    }

    @Inject(method = "getLightCoords", at = @At("HEAD"), cancellable = true)
    private void agoniaFishingQol$useFullBrightness(float partialTicks, CallbackInfoReturnable<Integer> cir) {
        if (agoniaFishingQol$fullBright) {
            cir.setReturnValue(LightCoordsUtil.FULL_BRIGHT);
        }
    }
}
