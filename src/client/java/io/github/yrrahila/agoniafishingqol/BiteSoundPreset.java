package io.github.yrrahila.agoniafishingqol;

import java.util.Locale;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public enum BiteSoundPreset {
    CHALLENGE("challenge", "Challenge", SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.60F, 0.78F, 44),
    PLING("pling", "Pling", SoundEvents.NOTE_BLOCK_PLING.value(), 1.45F, 1.35F, 14),
    XP("xp", "XP", SoundEvents.EXPERIENCE_ORB_PICKUP, 1.35F, 1.00F, 12),
    LEVEL_UP("level_up", "Level Up", SoundEvents.PLAYER_LEVELUP, 1.10F, 1.00F, 38),
    SNARE("snare", "Snare", SoundEvents.NOTE_BLOCK_SNARE.value(), 1.40F, 1.00F, 16);

    public static final BiteSoundPreset DEFAULT = CHALLENGE;

    private final String id;
    private final String displayName;
    private final SoundEvent sound;
    private final float volume;
    private final float pitch;
    private final int repeatIntervalTicks;

    BiteSoundPreset(
        String id,
        String displayName,
        SoundEvent sound,
        float volume,
        float pitch,
        int repeatIntervalTicks
    ) {
        this.id = id;
        this.displayName = displayName;
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.repeatIntervalTicks = repeatIntervalTicks;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public SoundEvent sound() {
        return sound;
    }

    public float volume() {
        return volume;
    }

    public float pitch() {
        return pitch;
    }

    public int repeatIntervalTicks() {
        return repeatIntervalTicks;
    }

    public BiteSoundPreset next(int direction) {
        BiteSoundPreset[] presets = values();
        return presets[Math.floorMod(ordinal() + direction, presets.length)];
    }

    public static BiteSoundPreset byId(String id) {
        if (id != null) {
            String normalized = id.toLowerCase(Locale.ROOT);
            for (BiteSoundPreset preset : values()) {
                if (preset.id.equals(normalized)) {
                    return preset;
                }
            }
        }
        return DEFAULT;
    }
}
