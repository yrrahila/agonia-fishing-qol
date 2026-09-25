package io.github.yrrahila.agoniafishingqol.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {
    @Inject(
        method = "createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void agoniaFishingQol$hideOrdinaryWaterBubbles(
        ParticleOptions options,
        double x,
        double y,
        double z,
        double xSpeed,
        double ySpeed,
        double zSpeed,
        CallbackInfoReturnable<Particle> cir
    ) {
        if (options.getType() == ParticleTypes.BUBBLE) {
            cir.setReturnValue(null);
            return;
        }

        if (options.getType() != ParticleTypes.SPLASH
            && options.getType() != ParticleTypes.UNDERWATER
            && options.getType() != ParticleTypes.BUBBLE_POP) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        FishingHook ownHook = client.player.fishing;
        if (ownHook == null || ownHook.isRemoved() || ownHook.getPlayerOwner() != client.player) {
            return;
        }

        double dx = ownHook.getX() - x;
        double dy = ownHook.getY() - y;
        double dz = ownHook.getZ() - z;
        if (dx * dx + dy * dy + dz * dz <= 16.0) {
            cir.setReturnValue(null);
        }
    }
}
