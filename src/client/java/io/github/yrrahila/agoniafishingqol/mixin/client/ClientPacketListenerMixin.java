package io.github.yrrahila.agoniafishingqol.mixin.client;

import io.github.yrrahila.agoniafishingqol.AgoniaFishingQolClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "handleParticleEvent", at = @At("HEAD"), cancellable = true)
    private void agoniaFishingQol$observeFishingParticles(ClientboundLevelParticlesPacket packet, CallbackInfo ci) {
        if (AgoniaFishingQolClient.tracker().handleParticlePacket(Minecraft.getInstance(), packet)) {
            ci.cancel();
        }
    }
}
