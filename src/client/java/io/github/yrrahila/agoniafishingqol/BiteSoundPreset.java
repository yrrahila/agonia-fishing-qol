package io.github.yrrahila.agoniafishingqol;

import java.util.Locale;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public enum BiteSoundPreset {
    CHALLENGE("challenge", "Challenge", SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.75F, 0.90F, 50),
    PLING("pling", "Pling", SoundEvents.NOTE_BLOCK_PLING.value(), 1.00F, 1.30F, 14),
    XP("xp", "XP", SoundEvents.EXPERIENCE_ORB_PICKUP, 0.90F, 1.00F, 12),
    LEVEL_UP("level_up", "Level Up", SoundEvents.PLAYER_LEVELUP, 0.70F, 1.00F, 42),
    SNARE("snare", "Snare", SoundEvents.NOTE_BLOCK_SNARE.value(), 1.00F, 1.05F, 16),
    CHIME("chime", "Chime", SoundEvents.NOTE_BLOCK_CHIME.value(), 0.85F, 1.15F, 22),
    XYLOPHONE("xylophone", "Xylophone", SoundEvents.NOTE_BLOCK_XYLOPHONE.value(), 1.00F, 1.20F, 18),
    IRON_CHIME("iron_chime", "Iron Chime", SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE.value(), 0.85F, 1.15F, 20),
    HARP("harp", "Harp", SoundEvents.NOTE_BLOCK_HARP.value(), 1.00F, 1.20F, 16),
    BIT("bit", "Bit", SoundEvents.NOTE_BLOCK_BIT.value(), 0.95F, 1.10F, 16),
    BASS("bass", "Bass", SoundEvents.NOTE_BLOCK_BASS.value(), 1.20F, 1.00F, 18),
    BASS_DRUM("bass_drum", "Bass Drum", SoundEvents.NOTE_BLOCK_BASEDRUM.value(), 1.15F, 1.10F, 20),
    HI_HAT("hi_hat", "Hi-Hat", SoundEvents.NOTE_BLOCK_HAT.value(), 1.10F, 1.00F, 14),
    BANJO("banjo", "Banjo", SoundEvents.NOTE_BLOCK_BANJO.value(), 1.00F, 1.10F, 18),
    TRUMPET("trumpet", "Trumpet", SoundEvents.NOTE_BLOCK_TRUMPET.value(), 0.80F, 1.00F, 24),
    CLICK("click", "Click", SoundEvents.UI_BUTTON_CLICK.value(), 1.10F, 1.10F, 14),
    POP("pop", "Pop", SoundEvents.ITEM_PICKUP, 0.90F, 1.20F, 14),
    ARROW_ALERT("arrow_alert", "Arrow Alert", SoundEvents.ARROW_HIT_PLAYER, 0.85F, 1.00F, 18),
    COPPER_PULSE("copper_pulse", "Copper Pulse", SoundEvents.COPPER_BULB_TURN_ON, 0.80F, 1.10F, 20),
    VAULT_ALERT("vault_alert", "Vault Alert", SoundEvents.VAULT_ACTIVATE, 0.75F, 1.10F, 24),
    DEFLECT("deflect", "Deflect", SoundEvents.BREEZE_DEFLECT, 0.75F, 1.10F, 20);

    public static final BiteSoundPreset DEFAULT = CHALLENGE;

    private final String id;
    private final String displayName;
    private final SoundEvent sound;
    private final float baseVolume;
    private final float pitch;
    private final int repeatIntervalTicks;

    BiteSoundPreset(
        String id,
        String displayName,
        SoundEvent sound,
        float baseVolume,
        float pitch,
        int repeatIntervalTicks
    ) {
        this.id = id;
        this.displayName = displayName;
        this.sound = sound;
        this.baseVolume = baseVolume;
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

    public float baseVolume() {
        return baseVolume;
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
