package io.github.yrrahila.agoniafishingqol;

import java.util.List;
import net.minecraft.world.phys.Vec3;

/** Duck interface mixed into the fishing-hook render state; kept outside the reserved mixin package. */
public interface FishingHookRenderStateAccess {
    void agoniaFishingQol$setOwnHook(boolean ownHook);

    boolean agoniaFishingQol$isOwnHook();

    void agoniaFishingQol$setApproaching(boolean approaching);

    boolean agoniaFishingQol$isApproaching();

    void agoniaFishingQol$setBiting(boolean biting);

    boolean agoniaFishingQol$isBiting();

    void agoniaFishingQol$setTrailPositions(List<Vec3> positions);

    List<Vec3> agoniaFishingQol$trailPositions();
}
