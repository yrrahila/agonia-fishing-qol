package io.github.yrrahila.agoniafishingqol;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class AgoniaFishingQolClient implements ClientModInitializer {
    public static final String MOD_ID = "agonia_fishing_qol";
    private static final FishingTracker TRACKER = new FishingTracker();
    private static final BiteSoundController BITE_SOUND_CONTROLLER = new BiteSoundController();
    private static BiteSoundConfig config;
    private static KeyMapping openSettings;

    public static FishingTracker tracker() {
        return TRACKER;
    }

    public static BiteSoundConfig config() {
        return config;
    }

    public static BiteSoundController biteSoundController() {
        return BITE_SOUND_CONTROLLER;
    }

    @Override
    public void onInitializeClient() {
        config = BiteSoundConfig.load();
        KeyMapping.Category category = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(MOD_ID, "settings")
        );
        openSettings = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.agonia_fishing_qol.open_settings",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,
            category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            TRACKER.tick(client);
            while (openSettings.consumeClick() && client.gui.screen() == null) {
                client.gui.setScreen(new AgoniaFishingSettingsScreen(null, config, BITE_SOUND_CONTROLLER));
            }
        });
        HudElementRegistry.attachElementAfter(
            VanillaHudElements.HOTBAR,
            Identifier.fromNamespaceAndPath(MOD_ID, "fishing_status"),
            (graphics, tickCounter) -> FishingHud.render(graphics, TRACKER.snapshot())
        );
    }
}
