package io.github.yrrahila.agoniafishingqol;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/** Mirrors FishingHook's vanilla 5x5x4 open-water scan and adds a concise failure reason. */
public final class OpenWaterEvaluator {
    private OpenWaterEvaluator() {
    }

    public static Result evaluate(FishingHook hook) {
        Level level = hook.level();
        BlockPos center = hook.blockPosition();
        LayerType previous = LayerType.INVALID;

        for (int y = -1; y <= 2; y++) {
            LayerResult layer = classifyLayer(level, center, y);
            if (layer.type() == LayerType.INVALID) {
                return new Result(false, reasonForInvalidLayer(layer, y), true);
            }
            if (layer.type() == LayerType.ABOVE_WATER && previous == LayerType.INVALID) {
                return new Result(false, "insufficient water area", true);
            }
            if (layer.type() == LayerType.INSIDE_WATER && previous == LayerType.ABOVE_WATER) {
                return new Result(false, "water above air gap", true);
            }
            previous = layer.type();
        }

        return new Result(true, "", true);
    }

    private static LayerResult classifyLayer(Level level, BlockPos center, int yOffset) {
        LayerType uniform = null;
        boolean solidAbove = false;
        boolean missingWater = false;

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos pos = center.offset(x, yOffset, z);
                BlockState state = level.getBlockState(pos);
                LayerType type = classifyBlock(level, pos, state);
                if (yOffset >= 1 && type == LayerType.INVALID) {
                    solidAbove = true;
                }
                if (yOffset <= 0 && type != LayerType.INSIDE_WATER) {
                    missingWater = true;
                }
                if (uniform == null) {
                    uniform = type;
                } else if (uniform != type) {
                    return new LayerResult(LayerType.INVALID, solidAbove, missingWater);
                }
            }
        }

        return new LayerResult(uniform == null ? LayerType.INVALID : uniform, solidAbove, missingWater);
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

    private static String reasonForInvalidLayer(LayerResult layer, int yOffset) {
        if (layer.solidAbove()) {
            return "block above bobber";
        }
        if (layer.missingWater() || yOffset <= 0) {
            return "insufficient source-water area";
        }
        if (yOffset >= 1) {
            return "invalid blocks above bobber";
        }
        return "invalid blocks nearby";
    }

    private enum LayerType {
        ABOVE_WATER,
        INSIDE_WATER,
        INVALID
    }

    private record LayerResult(LayerType type, boolean solidAbove, boolean missingWater) {
    }

    public record Result(boolean open, String reason, boolean known) {
        public static Result unknown() {
            return new Result(false, "", false);
        }
    }
}
