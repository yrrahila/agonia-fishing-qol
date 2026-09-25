package io.github.yrrahila.agoniafishingqol;

import io.github.yrrahila.agoniafishingqol.FishingSnapshot.BobberStatus;
import io.github.yrrahila.agoniafishingqol.mixin.client.FishingHookAccessor;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class FishingTracker {
    private static final int LOW_DURABILITY_THRESHOLD = 10;
    private static final int OPEN_WATER_REFRESH_TICKS = 10;

    private FishingHook activeHook;
    private long waterEntryTick = -1L;
    private boolean lastBiting;
    private boolean durabilityWarningShown;
    private int lureLevelAtCast;
    private OpenWaterEvaluator.Result openWater = OpenWaterEvaluator.Result.unknown();
    private FishingSnapshot snapshot = FishingSnapshot.NOT_CAST;

    public void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            reset();
            return;
        }

        warnForLowDurability(client);

        FishingHook ownHook = client.player.fishing;
        if (ownHook == null || ownHook.isRemoved() || ownHook.getPlayerOwner() != client.player) {
            clearPreviousHighlight();
            activeHook = null;
            waterEntryTick = -1L;
            lastBiting = false;
            openWater = OpenWaterEvaluator.Result.unknown();
            snapshot = FishingSnapshot.NOT_CAST;
            return;
        }

        if (ownHook != activeHook) {
            clearPreviousHighlight();
            activeHook = ownHook;
            waterEntryTick = -1L;
            lastBiting = false;
            lureLevelAtCast = readLureLevel(client, findHeldRod(client));
            openWater = OpenWaterEvaluator.Result.unknown();
        }

        activeHook.setGlowingTag(true);
        long gameTime = client.level.getGameTime();
        boolean inWater = client.level.getFluidState(activeHook.blockPosition()).is(FluidTags.WATER);
        if (inWater && waterEntryTick < 0L) {
            waterEntryTick = gameTime;
        } else if (!inWater) {
            waterEntryTick = -1L;
        }

        boolean biting = ((FishingHookAccessor) activeHook).agoniaFishingQol$isBiting();
        if (biting && !lastBiting) {
            client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.15F, 1.0F));
        } else if (!biting && lastBiting) {
            // A missed bite starts a fresh, server-randomized waiting cycle.
            waterEntryTick = inWater ? gameTime : -1L;
        }
        lastBiting = biting;

        if (gameTime % OPEN_WATER_REFRESH_TICKS == 0L) {
            openWater = OpenWaterEvaluator.evaluate(activeHook);
        }

        BobberStatus status = biting ? BobberStatus.BITE_READY : BobberStatus.WAITING;
        snapshot = new FishingSnapshot(status, estimateBite(client, gameTime, inWater, biting), openWater, biting);
    }

    public FishingSnapshot snapshot() {
        return snapshot;
    }

    private void warnForLowDurability(Minecraft client) {
        ItemStack rod = findHeldRod(client);
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
            return "Ready now";
        }
        if (!inWater || waterEntryTick < 0L) {
            return "Waiting for bobber to settle";
        }

        int lureReduction = Math.max(0, lureLevelAtCast) * 100;
        int minimumTicks = Math.max(0, 100 - lureReduction) + 20;
        int maximumTicks = Math.max(0, 600 - lureReduction) + 80;

        boolean rainBoost = client.level.isRainingAt(activeHook.blockPosition().above());
        boolean skyVisible = client.level.canSeeSky(activeHook.blockPosition().above());
        double expectedTickRate = 1.0 + (rainBoost ? 0.25 : 0.0) - (skyVisible ? 0.0 : 0.5);
        minimumTicks = (int) Math.ceil(minimumTicks / expectedTickRate);
        maximumTicks = (int) Math.ceil(maximumTicks / expectedTickRate);

        long elapsed = Math.max(0L, gameTime - waterEntryTick);
        double minSeconds = Math.max(0L, minimumTicks - elapsed) / 20.0;
        double maxSeconds = Math.max(0L, maximumTicks - elapsed) / 20.0;
        if (maxSeconds <= 5.0) {
            return "Bite expected soon (estimate)";
        }

        int minDisplay = (int) Math.floor(minSeconds);
        int maxDisplay = (int) Math.ceil(maxSeconds);
        String suffix = rainBoost ? " (rain estimate)" : " (estimate)";
        return String.format(Locale.ROOT, "Estimated bite: %d-%ds%s", minDisplay, maxDisplay, suffix);
    }

    private void clearPreviousHighlight() {
        if (activeHook != null && !activeHook.isRemoved()) {
            activeHook.setGlowingTag(false);
        }
    }

    private void reset() {
        clearPreviousHighlight();
        activeHook = null;
        waterEntryTick = -1L;
        lastBiting = false;
        durabilityWarningShown = false;
        openWater = OpenWaterEvaluator.Result.unknown();
        snapshot = FishingSnapshot.NOT_CAST;
    }
}
