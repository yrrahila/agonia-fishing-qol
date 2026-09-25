package io.github.yrrahila.agoniafishingqol;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import org.jspecify.annotations.Nullable;

public final class BiteSoundController {
    private static final int PREVIEW_DURATION_TICKS = 70;

    private boolean biteActive;
    private int biteCooldownTicks;
    private int previewRemainingTicks;
    private int previewCooldownTicks;
    private @Nullable BiteSoundPreset previewPreset;
    private double previewVolumeMultiplier = BiteSoundConfig.DEFAULT_ALERT_VOLUME;
    private @Nullable SimpleSoundInstance activeBiteSound;
    private @Nullable SimpleSoundInstance activePreviewSound;

    public void tickBite(
        Minecraft client,
        boolean biting,
        BiteSoundPreset preset,
        double volumeMultiplier
    ) {
        if (!biting) {
            stopBite(client);
            return;
        }

        if (!biteActive) {
            stopPreview(client);
            biteActive = true;
            biteCooldownTicks = 0;
        }

        if (biteCooldownTicks <= 0) {
            activeBiteSound = replaceSound(client, activeBiteSound, preset, volumeMultiplier);
            biteCooldownTicks = preset.repeatIntervalTicks();
        } else {
            biteCooldownTicks--;
        }
    }

    public void startPreview(Minecraft client, BiteSoundPreset preset, double volumeMultiplier) {
        if (biteActive || !AgoniaFishingQolClient.isEnabled()) {
            return;
        }
        stopPreview(client);
        previewPreset = preset;
        previewVolumeMultiplier = BiteSoundConfig.sanitizeForPlayback(volumeMultiplier);
        previewRemainingTicks = PREVIEW_DURATION_TICKS;
        activePreviewSound = replaceSound(client, null, preset, previewVolumeMultiplier);
        previewCooldownTicks = preset.repeatIntervalTicks();
    }

    public void tickPreview(Minecraft client) {
        if (biteActive || previewPreset == null || previewRemainingTicks <= 0) {
            stopPreview(client);
            return;
        }

        previewRemainingTicks--;
        if (previewCooldownTicks <= 0) {
            activePreviewSound = replaceSound(
                client,
                activePreviewSound,
                previewPreset,
                previewVolumeMultiplier
            );
            previewCooldownTicks = previewPreset.repeatIntervalTicks();
        } else {
            previewCooldownTicks--;
        }

        if (previewRemainingTicks <= 0) {
            stopPreview(client);
        }
    }

    public void stopBite(Minecraft client) {
        if (activeBiteSound != null) {
            client.getSoundManager().stop(activeBiteSound);
            activeBiteSound = null;
        }
        biteActive = false;
        biteCooldownTicks = 0;
    }

    public void stopPreview(Minecraft client) {
        if (activePreviewSound != null) {
            client.getSoundManager().stop(activePreviewSound);
            activePreviewSound = null;
        }
        previewPreset = null;
        previewVolumeMultiplier = BiteSoundConfig.DEFAULT_ALERT_VOLUME;
        previewRemainingTicks = 0;
        previewCooldownTicks = 0;
    }

    public void stopAll(Minecraft client) {
        stopBite(client);
        stopPreview(client);
    }

    private @Nullable SimpleSoundInstance replaceSound(
        Minecraft client,
        @Nullable SimpleSoundInstance previous,
        BiteSoundPreset preset,
        double volumeMultiplier
    ) {
        if (previous != null) {
            client.getSoundManager().stop(previous);
        }
        float finalVolume = (float)(preset.baseVolume()
            * BiteSoundConfig.sanitizeForPlayback(volumeMultiplier));
        if (finalVolume <= 0.0F) {
            return null;
        }
        SimpleSoundInstance next = SimpleSoundInstance.forUI(preset.sound(), preset.pitch(), finalVolume);
        client.getSoundManager().play(next);
        return next;
    }
}
