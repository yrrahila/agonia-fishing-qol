package io.github.yrrahila.agoniafishingqol;

import java.util.Objects;
import java.nio.file.Path;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.PackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Enables the clear-water textures as an ordinary built-in resource pack. */
public final class ClearWaterController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgoniaFishingQolClient.MOD_ID);
    private static final Identifier PACK_ID = Identifier.fromNamespaceAndPath(
        AgoniaFishingQolClient.MOD_ID,
        "clear_water"
    );
    private static final String PACK_REPOSITORY_ID = PACK_ID.toString();

    private boolean desiredEnabled;
    private Boolean appliedEnabled;
    private boolean reloadInProgress;

    public void register() {
        ModContainer container = FabricLoader.getInstance()
            .getModContainer(AgoniaFishingQolClient.MOD_ID)
            .orElseThrow(() -> new IllegalStateException("Missing Agonia Fishing QoL mod container"));
        boolean registered = ResourceLoader.registerBuiltinPack(
            PACK_ID,
            container,
            Component.literal("Agonia Clear Water"),
            PackActivationType.NORMAL
        );
        if (!registered) {
            for (Path root : container.getRootPaths()) {
                LOGGER.error("Clear-water pack lookup root: {}", root.resolve("resourcepacks/clear_water"));
            }
            throw new IllegalStateException("Could not register the Agonia clear-water resource pack");
        }
    }

    public void initialize(boolean enabled) {
        desiredEnabled = enabled;
        appliedEnabled = null;
    }

    public void setEnabled(boolean enabled) {
        desiredEnabled = enabled;
    }

    public void tick(Minecraft client) {
        if (reloadInProgress || Objects.equals(appliedEnabled, desiredEnabled)) {
            return;
        }

        PackRepository repository = client.getResourcePackRepository();
        repository.reload();
        boolean targetEnabled = desiredEnabled;
        boolean changed = targetEnabled
            ? repository.addPack(PACK_REPOSITORY_ID)
            : repository.removePack(PACK_REPOSITORY_ID);
        if (!changed) {
            appliedEnabled = targetEnabled;
            return;
        }

        client.options.updateResourcePacks(repository);
        reloadInProgress = true;
        client.reloadResourcePacks().whenComplete((ignored, error) -> client.execute(() -> {
            reloadInProgress = false;
            appliedEnabled = targetEnabled;
            if (error != null) {
                LOGGER.warn("Could not apply the Agonia clear-water resource state", error);
            }
        }));
    }
}
