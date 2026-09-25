package io.github.yrrahila.agoniafishingqol.mixin.client;

public interface FishingHookRenderStateExtension {
    void agoniaFishingQol$setOwnHook(boolean ownHook);

    boolean agoniaFishingQol$isOwnHook();

    void agoniaFishingQol$setBiting(boolean biting);

    boolean agoniaFishingQol$isBiting();
}
