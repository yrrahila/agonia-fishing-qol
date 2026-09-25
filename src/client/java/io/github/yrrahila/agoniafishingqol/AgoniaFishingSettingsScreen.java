package io.github.yrrahila.agoniafishingqol;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public final class AgoniaFishingSettingsScreen extends Screen {
    private static final Component TITLE = Component.literal("Agonia Fishing QoL");

    private final @Nullable Screen previousScreen;
    private final BiteSoundConfig config;
    private final BiteSoundController soundController;
    private BiteSoundPreset selectedPreset;
    private Button selectedSoundButton;

    public AgoniaFishingSettingsScreen(
        @Nullable Screen previousScreen,
        BiteSoundConfig config,
        BiteSoundController soundController
    ) {
        super(TITLE);
        this.previousScreen = previousScreen;
        this.config = config;
        this.soundController = soundController;
        this.selectedPreset = config.selectedPreset();
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int selectorY = height / 2 - 26;

        addRenderableWidget(Button.builder(Component.literal("<"), button -> cycle(-1))
            .bounds(centerX - 110, selectorY, 36, 20)
            .build());
        selectedSoundButton = addRenderableWidget(Button.builder(
            Component.literal(selectedPreset.displayName()),
            button -> {
            }
        ).bounds(centerX - 68, selectorY, 136, 20).build());
        selectedSoundButton.active = false;
        addRenderableWidget(Button.builder(Component.literal(">"), button -> cycle(1))
            .bounds(centerX + 74, selectorY, 36, 20)
            .build());

        addRenderableWidget(Button.builder(Component.literal("Preview"), button ->
            soundController.startPreview(minecraft, selectedPreset)
        ).bounds(centerX - 50, selectorY + 32, 100, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
            .bounds(centerX - 100, selectorY + 64, 200, 20)
            .build());
    }

    @Override
    public void tick() {
        soundController.tickPreview(minecraft);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
        super.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
        graphics.centeredText(font, TITLE, width / 2, height / 2 - 78, 0xFFFFFFFF);
        graphics.centeredText(font, Component.literal("Bite Sound"), width / 2, height / 2 - 50, 0xFFF2F2F2);
    }

    @Override
    public void onClose() {
        config.save();
        soundController.stopPreview(minecraft);
        minecraft.gui.setScreen(previousScreen);
    }

    @Override
    public void removed() {
        config.save();
        soundController.stopPreview(minecraft);
    }

    private void cycle(int direction) {
        soundController.stopPreview(minecraft);
        selectedPreset = selectedPreset.next(direction);
        config.select(selectedPreset);
        selectedSoundButton.setMessage(Component.literal(selectedPreset.displayName()));
    }
}
