package io.github.yrrahila.agoniafishingqol;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/** Mirrors FishingHook's vanilla 5x5x4 open-water scan. */
public final class OpenWaterEvaluator {
    private OpenWaterEvaluator() {
    }

    public static Result evaluate(FishingHook hook) {
        Level level = hook.level();
        BlockPos center = hook.blockPosition();
        LayerType previous = LayerType.INVALID;

        for (int y = -1; y <= 2; y++) {
            LayerType layer = classifyLayer(level, center, y);
            if (layer == LayerType.INVALID) {
                return new Result(false, true);
            }
            if (layer == LayerType.ABOVE_WATER && previous == LayerType.INVALID) {
                return new Result(false, true);
            }
            if (layer == LayerType.INSIDE_WATER && previous == LayerType.ABOVE_WATER) {
                return new Result(false, true);
            }
            previous = layer;
        }

        return new Result(true, true);
    }

    private static LayerType classifyLayer(Level level, BlockPos center, int yOffset) {
        LayerType uniform = null;

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos pos = center.offset(x, yOffset, z);
                BlockState state = level.getBlockState(pos);
                LayerType type = classifyBlock(level, pos, state);
                if (uniform == null) {
                    uniform = type;
                } else if (uniform != type) {
                    return LayerType.INVALID;
                }
            }
        }

        return uniform == null ? LayerType.INVALID : uniform;
    }

    private static LayerType classifyBlock(Level level, BlockPos pos, BlockState state) {
        if (state.isAir() || state.is(Blocks.LILY_PAD)) {
            return LayerType.ABOVE_WATER;
        }

        FluidState fluid = state.getFluidState();
        if (fluid.is(FluidTags.WATER) && fluid.isSource() && state.getCollisionShape(level, pos).isEmpty()) {
            return LayerType.INSIDE_WATER;
        }
        return LayerType.INVALID;
    }

    private enum LayerType {
        ABOVE_WATER,
        INSIDE_WATER,
        INVALID
    }

    public record Result(boolean open, boolean known) {
        public static Result unknown() {
            return new Result(false, false);
        }
    }
}
