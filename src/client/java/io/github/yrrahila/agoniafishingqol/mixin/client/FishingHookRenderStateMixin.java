package io.github.yrrahila.agoniafishingqol.mixin.client;

import io.github.yrrahila.agoniafishingqol.FishingHookRenderStateAccess;
import io.github.yrrahila.agoniafishingqol.FishingVisualState;
import java.util.List;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(FishingHookRenderState.class)
public final class FishingHookRenderStateMixin implements FishingHookRenderStateAccess {
    @Unique
    private boolean agoniaFishingQol$ownHook;

    @Unique
    private boolean agoniaFishingQol$approaching;

    @Unique
    private boolean agoniaFishingQol$biting;

    @Unique
    private float agoniaFishingQol$biteScaleProgress;

    @Unique
    private List<FishingVisualState.TrailSpan> agoniaFishingQol$trailSpans = List.of();

    @Override
    public void agoniaFishingQol$setOwnHook(boolean ownHook) {
        this.agoniaFishingQol$ownHook = ownHook;
    }

    @Override
    public boolean agoniaFishingQol$isOwnHook() {
        return this.agoniaFishingQol$ownHook;
    }

    @Override
    public void agoniaFishingQol$setApproaching(boolean approaching) {
        this.agoniaFishingQol$approaching = approaching;
    }

    @Override
    public boolean agoniaFishingQol$isApproaching() {
        return this.agoniaFishingQol$approaching;
    }

    @Override
    public void agoniaFishingQol$setBiting(boolean biting) {
        this.agoniaFishingQol$biting = biting;
    }

    @Override
    public boolean agoniaFishingQol$isBiting() {
        return this.agoniaFishingQol$biting;
    }

    @Override
    public void agoniaFishingQol$setBiteScaleProgress(float progress) {
        this.agoniaFishingQol$biteScaleProgress = progress;
    }

    @Override
    public float agoniaFishingQol$biteScaleProgress() {
        return this.agoniaFishingQol$biteScaleProgress;
    }

    @Override
    public void agoniaFishingQol$setTrailSpans(List<FishingVisualState.TrailSpan> spans) {
        this.agoniaFishingQol$trailSpans = spans;
    }

    @Override
    public List<FishingVisualState.TrailSpan> agoniaFishingQol$trailSpans() {
        return this.agoniaFishingQol$trailSpans;
    }
}
