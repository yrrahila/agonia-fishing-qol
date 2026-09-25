package io.github.yrrahila.agoniafishingqol;

import java.util.List;

/** Duck interface mixed into the fishing-hook render state; kept outside the reserved mixin package. */
public interface FishingHookRenderStateAccess {
    void agoniaFishingQol$setOwnHook(boolean ownHook);

    boolean agoniaFishingQol$isOwnHook();

    void agoniaFishingQol$setApproaching(boolean approaching);

    boolean agoniaFishingQol$isApproaching();

    void agoniaFishingQol$setBiting(boolean biting);

    boolean agoniaFishingQol$isBiting();

    void agoniaFishingQol$setBiteScaleProgress(float progress);

    float agoniaFishingQol$biteScaleProgress();

    void agoniaFishingQol$setTrailSpans(List<FishingVisualState.TrailSpan> spans);

    List<FishingVisualState.TrailSpan> agoniaFishingQol$trailSpans();
}
