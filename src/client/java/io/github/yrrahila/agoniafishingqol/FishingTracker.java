package io.github.yrrahila.agoniafishingqol;

import io.github.yrrahila.agoniafishingqol.FishingSnapshot.BobberStatus;
import io.github.yrrahila.agoniafishingqol.mixin.client.FishingHookAccessor;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class FishingTracker {
    private static final int LOW_DURABILITY_THRESHOLD = 10;
    private static final int APPROACH_STATE_GRACE_TICKS = 4;
    private static final int SETTLE_TICKS_BEFORE_ANCHOR = 12;
    private static final float ANCHOR_BLEND_PER_TICK = 1.0F / 8.0F;
    private static final float APPROACH_SOUND_PITCH = 1.10F;
    private static final float APPROACH_SOUND_VOLUME = 1.55F;
    private static final float BITE_SOUND_PITCH = 0.78F;
    private static final float BITE_SOUND_VOLUME = 1.90F;

    private FishingHook activeHook;
    private long biteCycleStartTick = -1L;
    private long approachingUntilTick = Long.MIN_VALUE;
    private boolean lastApproaching;
    private boolean lastBiting;
    private boolean durabilityWarningShown;
    private int lureLevelAtCast;
    private int inWaterTicks;
    private float previousAnchorWeight;
    private float anchorWeight;
    private @Nullable Vec3 visualAnchor;
    private @Nullable SimpleSoundInstance activeApproachSound;
    private @Nullable SimpleSoundInstance activeBiteSound;
    private FishingSnapshot snapshot = FishingSnapshot.NOT_CAST;

    public void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            reset(client);
            return;
        }

        ItemStack heldRod = findHeldRod(client);
        warnForLowDurability(client, heldRod);
        RodDurability durability = readDurability(heldRod);

        FishingHook ownHook = client.player.fishing;
        if (ownHook == null || ownHook.isRemoved() || ownHook.getPlayerOwner() != client.player) {
            clearFishingState(client);
            snapshot = FishingSnapshot.notCast(durability.remaining(), durability.maximum());
            return;
        }

        if (ownHook != activeHook) {
            clearFishingState(client);
            activeHook = ownHook;
            lureLevelAtCast = readLureLevel(client, heldRod);
        }

        activeHook.setGlowingTag(true);
        long gameTime = client.level.getGameTime();
        boolean inWater = client.level.getFluidState(activeHook.blockPosition()).is(FluidTags.WATER);
        boolean biting = ((FishingHookAccessor)activeHook).agoniaFishingQol$isBiting();

        if (lastBiting && !biting) {
            resetVisualAnchor();
        }
        if (inWater) {
            if (biteCycleStartTick < 0L) {
                biteCycleStartTick = gameTime;
            }
            if (!biting) {
                inWaterTicks++;
                if (visualAnchor == null && inWaterTicks >= SETTLE_TICKS_BEFORE_ANCHOR) {
                    visualAnchor = activeHook.position();
                }
            } else {
                inWaterTicks = 0;
            }
        } else {
            biteCycleStartTick = -1L;
            resetVisualAnchor();
        }

        boolean approaching = !biting && gameTime <= approachingUntilTick;

        if (approaching && !lastApproaching) {
            playApproachSound(client);
        } else if (!approaching && lastApproaching) {
            stopApproachSound(client);
        }

        if (biting) {
            stopApproachSound(client);
            approachingUntilTick = Long.MIN_VALUE;
            if (!lastBiting) {
                playBiteSound(client);
            }
        } else {
            stopBiteSound(client);
            if (lastBiting) {
                biteCycleStartTick = inWater ? gameTime : -1L;
            }
        }

        updateAnchorWeight(!biting && visualAnchor != null);
        FishingVisualState.publish(
            activeHook,
            approaching,
            biting,
            visualAnchor,
            previousAnchorWeight,
            anchorWeight
        );
        BobberStatus status = biting
            ? BobberStatus.BITE_READY
            : approaching ? BobberStatus.FISH_APPROACHING : BobberStatus.WAITING;
        snapshot = new FishingSnapshot(
            status,
            estimateBite(client, gameTime, inWater, biting),
            durability.remaining(),
            durability.maximum(),
            biting
        );
        lastApproaching = approaching;
        lastBiting = biting;
    }

    /** Returns true only for a fishing particle safely attributable to the local player's hook. */
    public boolean handleParticlePacket(Minecraft client, ClientboundLevelParticlesPacket packet) {
        if (client.player == null || client.level == null) {
            return false;
        }

        FishingHook ownHook = client.player.fishing;
        if (ownHook == null || ownHook.isRemoved() || ownHook.getPlayerOwner() != client.player) {
            return false;
        }

        if (isApproachTrailPacket(packet) && approachTrailBelongsToOwnHook(client, ownHook, packet)) {
            approachingUntilTick = client.level.getGameTime() + APPROACH_STATE_GRACE_TICKS;
            enhanceApproachTrail(client, packet);
            return false;
        }

        if (isCloseBiteFishingPacket(packet)) {
            return closeFishingEffectBelongsToOwnHook(client, ownHook, packet);
        }

        return isIdleFishingSplash(packet) && idleSplashBelongsToOwnHook(client, ownHook, packet);
    }

    public FishingSnapshot snapshot() {
        return snapshot;
    }

    private boolean isApproachTrailPacket(ClientboundLevelParticlesPacket packet) {
        return packet.getParticle().getType() == ParticleTypes.FISHING
            && packet.getCount() == 0
            && approximately(packet.getMaxSpeed(), 1.0F)
            && approximately(packet.getYDist(), 0.01F);
    }

    private boolean isCloseBiteFishingPacket(ClientboundLevelParticlesPacket packet) {
        return packet.getParticle().getType() == ParticleTypes.FISHING
            && packet.getCount() >= 1
            && approximately(packet.getYDist(), 0.0F)
            && approximately(packet.getMaxSpeed(), 0.2F)
            && packet.getXDist() > 0.0F
            && packet.getXDist() <= 0.5F
            && approximately(packet.getXDist(), packet.getZDist());
    }

    private boolean isIdleFishingSplash(ClientboundLevelParticlesPacket packet) {
        return packet.getParticle().getType() == ParticleTypes.SPLASH
            && (packet.getCount() == 2 || packet.getCount() == 3)
            && approximately(packet.getXDist(), 0.1F)
            && approximately(packet.getYDist(), 0.0F)
            && approximately(packet.getZDist(), 0.1F)
            && approximately(packet.getMaxSpeed(), 0.0F);
    }

    private boolean approachTrailBelongsToOwnHook(
        Minecraft client,
        FishingHook ownHook,
        ClientboundLevelParticlesPacket packet
    ) {
        if (!isFishingEffectCandidate(ownHook, packet, 0.0, 8.25)) {
            return false;
        }

        double ownError = trajectoryAlignmentError(ownHook, packet);
        if (ownError > 0.15) {
            return false;
        }

        AABB search = particleSearchBox(packet, 9.25);
        for (FishingHook hook : client.level.getEntitiesOfClass(FishingHook.class, search, hook -> !hook.isRemoved())) {
            if (hook == ownHook || !isFishingEffectCandidate(hook, packet, 0.0, 8.25)) {
                continue;
            }
            if (trajectoryAlignmentError(hook, packet) <= ownError + 0.08) {
                return false;
            }
        }
        return true;
    }

    private boolean closeFishingEffectBelongsToOwnHook(
        Minecraft client,
        FishingHook ownHook,
        ClientboundLevelParticlesPacket packet
    ) {
        double ownScore = closeFishingEffectScore(ownHook, packet);
        if (ownScore > 1.25) {
            return false;
        }

        AABB search = particleSearchBox(packet, 2.0);
        for (FishingHook hook : client.level.getEntitiesOfClass(FishingHook.class, search, hook -> !hook.isRemoved())) {
            if (hook != ownHook && closeFishingEffectScore(hook, packet) <= ownScore + 0.1) {
                return false;
            }
        }
        return true;
    }

    private double closeFishingEffectScore(FishingHook hook, ClientboundLevelParticlesPacket packet) {
        return horizontalDistanceSquared(hook, packet.getX(), packet.getZ())
            + Mth.square(packet.getY() - (hook.getY() + 0.5));
    }

    private boolean idleSplashBelongsToOwnHook(
        Minecraft client,
        FishingHook ownHook,
        ClientboundLevelParticlesPacket packet
    ) {
        if (!isFishingEffectCandidate(ownHook, packet, 2.25, 6.25)) {
            return false;
        }

        double ownDistance = horizontalDistanceSquared(ownHook, packet.getX(), packet.getZ());
        double expectedY = Mth.floor(ownHook.getY()) + 1.0;
        double ownScore = ownDistance + Mth.square(packet.getY() - expectedY);
        AABB search = particleSearchBox(packet, 7.25);
        for (FishingHook hook : client.level.getEntitiesOfClass(FishingHook.class, search, hook -> !hook.isRemoved())) {
            if (hook == ownHook || !isFishingEffectCandidate(hook, packet, 2.25, 6.25)) {
                continue;
            }
            double hookExpectedY = Mth.floor(hook.getY()) + 1.0;
            double score = horizontalDistanceSquared(hook, packet.getX(), packet.getZ())
                + Mth.square(packet.getY() - hookExpectedY);
            if (score <= ownScore + 0.25) {
                return false;
            }
        }
        return true;
    }

    private boolean isFishingEffectCandidate(
        FishingHook hook,
        ClientboundLevelParticlesPacket packet,
        double minimumHorizontalDistance,
        double maximumHorizontalDistance
    ) {
        double distance = horizontalDistanceSquared(hook, packet.getX(), packet.getZ());
        if (distance < Mth.square(minimumHorizontalDistance) || distance > Mth.square(maximumHorizontalDistance)) {
            return false;
        }
        double expectedY = Mth.floor(hook.getY()) + 1.0;
        return Math.abs(packet.getY() - expectedY) <= 0.35;
    }

    private double trajectoryAlignmentError(FishingHook hook, ClientboundLevelParticlesPacket packet) {
        double radialX = hook.getX() - packet.getX();
        double radialZ = hook.getZ() - packet.getZ();
        double tangentX = packet.getXDist();
        double tangentZ = packet.getZDist();
        double denominator = Math.sqrt(radialX * radialX + radialZ * radialZ)
            * Math.sqrt(tangentX * tangentX + tangentZ * tangentZ);
        if (denominator < 1.0E-6) {
            return Double.POSITIVE_INFINITY;
        }
        return Math.abs(radialX * tangentX + radialZ * tangentZ) / denominator;
    }

    private AABB particleSearchBox(ClientboundLevelParticlesPacket packet, double radius) {
        return new AABB(packet.getX(), packet.getY(), packet.getZ(), packet.getX(), packet.getY(), packet.getZ())
            .inflate(radius);
    }

    private double horizontalDistanceSquared(FishingHook hook, double x, double z) {
        return Mth.square(hook.getX() - x) + Mth.square(hook.getZ() - z);
    }

    private void enhanceApproachTrail(Minecraft client, ClientboundLevelParticlesPacket packet) {
        double xa = packet.getMaxSpeed() * packet.getXDist();
        double ya = packet.getMaxSpeed() * packet.getYDist();
        double za = packet.getMaxSpeed() * packet.getZDist();
        double[][] offsets = {
            {0.035, 0.0, 0.0},
            {-0.035, 0.0, 0.0},
            {0.0, 0.015, 0.035},
            {0.0, 0.015, -0.035}
        };

        for (double[] offset : offsets) {
            Particle particle = client.particleEngine.createParticle(
                ParticleTypes.FISHING,
                packet.getX() + offset[0],
                packet.getY() + offset[1],
                packet.getZ() + offset[2],
                xa,
                ya,
                za
            );
            if (particle != null) {
                particle.scale(2.25F);
            }
        }
    }

    private void playApproachSound(Minecraft client) {
        stopApproachSound(client);
        activeApproachSound = SimpleSoundInstance.forUI(
            SoundEvents.NOTE_BLOCK_BIT.value(),
            APPROACH_SOUND_PITCH,
            APPROACH_SOUND_VOLUME
        );
        client.getSoundManager().play(activeApproachSound);
    }

    private void stopApproachSound(Minecraft client) {
        if (activeApproachSound != null) {
            client.getSoundManager().stop(activeApproachSound);
            activeApproachSound = null;
        }
    }

    private void playBiteSound(Minecraft client) {
        stopBiteSound(client);
        activeBiteSound = SimpleSoundInstance.forUI(
            SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
            BITE_SOUND_PITCH,
            BITE_SOUND_VOLUME
        );
        client.getSoundManager().play(activeBiteSound);
    }

    private void stopBiteSound(Minecraft client) {
        if (activeBiteSound != null) {
            client.getSoundManager().stop(activeBiteSound);
            activeBiteSound = null;
        }
    }

    private void warnForLowDurability(Minecraft client, ItemStack rod) {
        if (rod.isEmpty() || !rod.isDamageableItem()) {
            durabilityWarningShown = false;
            return;
        }

        int remaining = rod.getMaxDamage() - rod.getDamageValue();
        if (remaining > LOW_DURABILITY_THRESHOLD) {
            durabilityWarningShown = false;
        } else if (!durabilityWarningShown) {
            client.player.sendOverlayMessage(
                Component.literal("Fishing rod low: " + remaining + " durability remaining").withStyle(ChatFormatting.GOLD)
            );
            durabilityWarningShown = true;
        }
    }

    private ItemStack findHeldRod(Minecraft client) {
        ItemStack main = client.player.getMainHandItem();
        if (main.is(Items.FISHING_ROD)) {
            return main;
        }
        ItemStack offhand = client.player.getOffhandItem();
        return offhand.is(Items.FISHING_ROD) ? offhand : ItemStack.EMPTY;
    }

    private RodDurability readDurability(ItemStack rod) {
        if (rod.isEmpty() || !rod.isDamageableItem()) {
            return new RodDurability(-1, -1);
        }
        return new RodDurability(rod.getMaxDamage() - rod.getDamageValue(), rod.getMaxDamage());
    }

    private int readLureLevel(Minecraft client, ItemStack rod) {
        if (rod.isEmpty()) {
            return 0;
        }
        try {
            Holder<Enchantment> lure = client.level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.LURE);
            return EnchantmentHelper.getItemEnchantmentLevel(lure, rod);
        } catch (RuntimeException ignored) {
            return 0;
        }
    }

    private String estimateBite(Minecraft client, long gameTime, boolean inWater, boolean biting) {
        if (biting) {
            return "Ready";
        }
        if (!inWater || biteCycleStartTick < 0L) {
            return "Settling";
        }

        int lureReduction = Math.max(0, lureLevelAtCast) * 100;
        int minimumTicks = Math.max(0, 100 - lureReduction) + 20;
        int maximumTicks = Math.max(0, 600 - lureReduction) + 80;

        boolean rainBoost = client.level.isRainingAt(activeHook.blockPosition().above());
        boolean skyVisible = client.level.canSeeSky(activeHook.blockPosition().above());
        double expectedTickRate = 1.0 + (rainBoost ? 0.25 : 0.0) - (skyVisible ? 0.0 : 0.5);
        minimumTicks = (int)Math.ceil(minimumTicks / expectedTickRate);
        maximumTicks = (int)Math.ceil(maximumTicks / expectedTickRate);

        long elapsed = Math.max(0L, gameTime - biteCycleStartTick);
        double minSeconds = Math.max(0L, minimumTicks - elapsed) / 20.0;
        double maxSeconds = Math.max(0L, maximumTicks - elapsed) / 20.0;
        if (maxSeconds <= 5.0) {
            return "Soon";
        }
        return String.format(Locale.ROOT, "%d-%ds", (int)Math.floor(minSeconds), (int)Math.ceil(maxSeconds));
    }

    private boolean approximately(float actual, float expected) {
        return Math.abs(actual - expected) < 0.0001F;
    }

    private void clearPreviousHighlight() {
        if (activeHook != null && !activeHook.isRemoved()) {
            activeHook.setGlowingTag(false);
        }
    }

    private void clearFishingState(Minecraft client) {
        clearPreviousHighlight();
        stopApproachSound(client);
        stopBiteSound(client);
        FishingVisualState.clear();
        activeHook = null;
        biteCycleStartTick = -1L;
        approachingUntilTick = Long.MIN_VALUE;
        lastApproaching = false;
        lastBiting = false;
        resetVisualAnchor();
    }

    private void updateAnchorWeight(boolean anchored) {
        previousAnchorWeight = anchorWeight;
        float target = anchored ? 1.0F : 0.0F;
        anchorWeight = Mth.clamp(
            anchorWeight + Mth.clamp(target - anchorWeight, -ANCHOR_BLEND_PER_TICK, ANCHOR_BLEND_PER_TICK),
            0.0F,
            1.0F
        );
    }

    private void resetVisualAnchor() {
        inWaterTicks = 0;
        previousAnchorWeight = 0.0F;
        anchorWeight = 0.0F;
        visualAnchor = null;
    }

    private void reset(Minecraft client) {
        clearFishingState(client);
        durabilityWarningShown = false;
        snapshot = FishingSnapshot.NOT_CAST;
    }

    private record RodDurability(int remaining, int maximum) {
    }
}
