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

    private final Path path;
    private BiteSoundPreset selectedPreset;

    private BiteSoundConfig(Path path, BiteSoundPreset selectedPreset) {
        this.path = path;
        this.selectedPreset = selectedPreset;
    }

    public static BiteSoundConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        BiteSoundConfig config = new BiteSoundConfig(path, BiteSoundPreset.DEFAULT);
        if (!Files.isRegularFile(path)) {
            config.save();
            return config;
        }

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            String id = data == null ? null : data.biteSound();
            config.selectedPreset = BiteSoundPreset.byId(id);
            if (id == null || !config.selectedPreset.id().equals(id)) {
                config.save();
            }
        } catch (RuntimeException | IOException exception) {
            LOGGER.warn("Could not read {}; using the default Bite sound", path, exception);
            config.selectedPreset = BiteSoundPreset.DEFAULT;
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

    public void save() {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(
                path,
                GSON.toJson(new ConfigData(selectedPreset.id())),
                StandardCharsets.UTF_8
            );
        } catch (IOException exception) {
            LOGGER.warn("Could not save Bite sound configuration to {}", path, exception);
        }
    }

    private record ConfigData(String biteSound) {
    }
}
