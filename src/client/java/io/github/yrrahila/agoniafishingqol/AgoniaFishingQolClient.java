package io.github.yrrahila.agoniafishingqol;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;

public final class AgoniaFishingQolClient implements ClientModInitializer {
    public static final String MOD_ID = "agonia_fishing_qol";
    private static final FishingTracker TRACKER = new FishingTracker();
    private static final BiteSoundController BITE_SOUND_CONTROLLER = new BiteSoundController();
    private static final ClearWaterController CLEAR_WATER_CONTROLLER = new ClearWaterController();
    private static BiteSoundConfig config;

    public static FishingTracker tracker() {
        return TRACKER;
    }

    public static BiteSoundConfig config() {
        return config;
    }

    public static BiteSoundController biteSoundController() {
        return BITE_SOUND_CONTROLLER;
    }

    public static boolean isEnabled() {
        return config != null && config.enabled();
    }

    public static void setEnabled(boolean enabled) {
        config.setEnabled(enabled);
        Minecraft client = Minecraft.getInstance();
        BITE_SOUND_CONTROLLER.stopAll(client);
        TRACKER.resetRuntimeState(client);
        CLEAR_WATER_CONTROLLER.setEnabled(enabled);
    }

    @Override
    public void onInitializeClient() {
        config = BiteSoundConfig.load();
        CLEAR_WATER_CONTROLLER.register();
        CLEAR_WATER_CONTROLLER.initialize(config.enabled());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            CLEAR_WATER_CONTROLLER.tick(client);
            TRACKER.tick(client);
        });
        HudElementRegistry.attachElementAfter(
            VanillaHudElements.HOTBAR,
            Identifier.fromNamespaceAndPath(MOD_ID, "fishing_status"),
            (graphics, tickCounter) -> {
                if (isEnabled()) {
                    FishingHud.render(graphics, TRACKER.snapshot());
                }
            }
        );
    }
}
