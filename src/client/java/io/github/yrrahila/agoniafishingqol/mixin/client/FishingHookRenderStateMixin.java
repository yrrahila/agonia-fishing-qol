package io.github.yrrahila.agoniafishingqol.mixin.client;

import io.github.yrrahila.agoniafishingqol.FishingHookRenderStateAccess;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(FishingHookRenderState.class)
public final class FishingHookRenderStateMixin implements FishingHookRenderStateAccess {
    @Unique
    private boolean agoniaFishingQol$ownHook;

    @Unique
    private boolean agoniaFishingQol$biting;

    @Override
    public void agoniaFishingQol$setOwnHook(boolean ownHook) {
        this.agoniaFishingQol$ownHook = ownHook;
    }

    @Override
    public boolean agoniaFishingQol$isOwnHook() {
        return this.agoniaFishingQol$ownHook;
    }

    @Override
    public void agoniaFishingQol$setBiting(boolean biting) {
        this.agoniaFishingQol$biting = biting;
    }

    @Override
    public boolean agoniaFishingQol$isBiting() {
        return this.agoniaFishingQol$biting;
    }
}
