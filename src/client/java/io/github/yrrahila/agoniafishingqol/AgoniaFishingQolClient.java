package io.github.yrrahila.agoniafishingqol;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;

public final class AgoniaFishingQolClient implements ClientModInitializer {
    public static final String MOD_ID = "agonia_fishing_qol";
    private static final FishingTracker TRACKER = new FishingTracker();

    public static FishingTracker tracker() {
        return TRACKER;
    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(TRACKER::tick);
        HudElementRegistry.attachElementAfter(
            VanillaHudElements.HOTBAR,
            Identifier.fromNamespaceAndPath(MOD_ID, "fishing_status"),
            (graphics, tickCounter) -> FishingHud.render(graphics, TRACKER.snapshot())
        );
    }
}
