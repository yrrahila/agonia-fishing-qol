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
        Minecraft client = Minecraft.getInstance();
        // This injection runs before vanilla's packet-thread handoff. Let vanilla schedule the
        // packet first, then inspect/cancel it when handleParticleEvent runs on the client thread.
        if (!client.isSameThread()) {
            return;
        }

        if (AgoniaFishingQolClient.tracker().handleParticlePacket(client, packet)) {
            ci.cancel();
        }
    }
}
