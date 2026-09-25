package io.github.yrrahila.agoniafishingqol;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;

public final class AgoniaFishingQolClient implements ClientModInitializer {
    public static final String MOD_ID = "agonia_fishing_qol";
    private static final FishingTracker TRACKER = new FishingTracker();
    private static final BiteSoundController BITE_SOUND_CONTROLLER = new BiteSoundController();
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

    @Override
    public void onInitializeClient() {
        config = BiteSoundConfig.load();
        ClientTickEvents.END_CLIENT_TICK.register(TRACKER::tick);
        HudElementRegistry.attachElementAfter(
            VanillaHudElements.HOTBAR,
            Identifier.fromNamespaceAndPath(MOD_ID, "fishing_status"),
            (graphics, tickCounter) -> FishingHud.render(graphics, TRACKER.snapshot())
        );
    }
}
