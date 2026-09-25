package io.github.yrrahila.agoniafishingqol.mixin.client;

import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FishingHook.class)
public interface FishingHookAccessor {
    @Accessor("biting")
    boolean agoniaFishingQol$isBiting();
}
