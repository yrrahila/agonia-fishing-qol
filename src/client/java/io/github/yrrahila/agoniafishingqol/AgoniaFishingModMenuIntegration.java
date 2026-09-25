package io.github.yrrahila.agoniafishingqol;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class AgoniaFishingModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new AgoniaFishingSettingsScreen(
            parent,
            AgoniaFishingQolClient.config(),
            AgoniaFishingQolClient.biteSoundController()
        );
    }
}
