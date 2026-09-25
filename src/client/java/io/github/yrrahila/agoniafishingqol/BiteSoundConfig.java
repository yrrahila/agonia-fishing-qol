package io.github.yrrahila.agoniafishingqol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BiteSoundConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgoniaFishingQolClient.MOD_ID);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "agonia-fishing-qol.json";
    public static final double DEFAULT_ALERT_VOLUME = 1.0;
    public static final double MIN_ALERT_VOLUME = 0.0;
    public static final double MAX_ALERT_VOLUME = 2.0;

    private final Path path;
    private BiteSoundPreset selectedPreset;
    private double alertVolume;
    private boolean enabled;

    private BiteSoundConfig(
        Path path,
        BiteSoundPreset selectedPreset,
        double alertVolume,
        boolean enabled
    ) {
        this.path = path;
        this.selectedPreset = selectedPreset;
        this.alertVolume = alertVolume;
        this.enabled = enabled;
    }

    public static BiteSoundConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        BiteSoundConfig config = new BiteSoundConfig(
            path,
            BiteSoundPreset.DEFAULT,
            DEFAULT_ALERT_VOLUME,
            true
        );
        if (!Files.isRegularFile(path)) {
            config.save();
            return config;
        }

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            String id = data == null ? null : data.biteSound();
            config.selectedPreset = BiteSoundPreset.byId(id);
            config.alertVolume = sanitizeVolume(data == null ? null : data.alertVolume());
            config.enabled = data == null || data.enabled() == null || data.enabled();
            if (id == null
                || !config.selectedPreset.id().equals(id)
                || data.alertVolume() == null
                || !Double.isFinite(data.alertVolume())
                || data.alertVolume() < MIN_ALERT_VOLUME
                || data.alertVolume() > MAX_ALERT_VOLUME
                || data.enabled() == null) {
                config.save();
            }
        } catch (RuntimeException | IOException exception) {
            LOGGER.warn("Could not read {}; using default settings", path, exception);
            config.selectedPreset = BiteSoundPreset.DEFAULT;
            config.alertVolume = DEFAULT_ALERT_VOLUME;
            config.enabled = true;
            config.save();
        }
        return config;
    }

    public BiteSoundPreset selectedPreset() {
        return selectedPreset;
    }

    public void select(BiteSoundPreset preset) {
        selectedPreset = preset;
        save();
    }

    public double alertVolume() {
        return alertVolume;
    }

    public void setAlertVolume(double volume) {
        alertVolume = sanitizeVolume(volume);
        save();
    }

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        save();
    }

    public void save() {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(
                path,
                GSON.toJson(new ConfigData(selectedPreset.id(), alertVolume, enabled)),
                StandardCharsets.UTF_8
            );
        } catch (IOException exception) {
            LOGGER.warn("Could not save Bite sound configuration to {}", path, exception);
        }
    }

    private static double sanitizeVolume(Double volume) {
        if (volume == null || !Double.isFinite(volume)) {
            return DEFAULT_ALERT_VOLUME;
        }
        return Math.clamp(volume, MIN_ALERT_VOLUME, MAX_ALERT_VOLUME);
    }

    static double sanitizeForPlayback(double volume) {
        return sanitizeVolume(volume);
    }

    private record ConfigData(String biteSound, Double alertVolume, Boolean enabled) {
    }
}
