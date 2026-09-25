package io.github.yrrahila.agoniafishingqol;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class FishingHud {
    private static final int PANEL_BACKGROUND = 0x90000000;
    private static final int TEXT = 0xFFF2F2F2;
    private static final int MUTED = 0xFFAAAAAA;
    private static final int GOOD = 0xFF55FF55;
    private static final int BAD = 0xFFFF7777;
    private static final int READY = 0xFFFFD34E;

    private FishingHud() {
    }

    public static void render(GuiGraphicsExtractor graphics, FishingSnapshot snapshot) {
        Minecraft client = Minecraft.getInstance();
        Font font = client.font;
        List<Line> lines = new ArrayList<>();
        int stateColor = snapshot.biteReady() ? READY : snapshot.status() == FishingSnapshot.BobberStatus.NOT_CAST ? MUTED : TEXT;
        lines.add(new Line("Fishing: " + snapshot.status().label(), stateColor));

        if (snapshot.status() != FishingSnapshot.BobberStatus.NOT_CAST) {
            lines.add(new Line(snapshot.estimatedBite(), snapshot.biteReady() ? READY : TEXT));
            if (snapshot.openWater().known()) {
                if (snapshot.openWater().open()) {
                    lines.add(new Line("Open Water ✓", GOOD));
                } else {
                    lines.add(new Line("Not Open Water ✗: " + snapshot.openWater().reason(), BAD));
                }
            }
        }

        int width = lines.stream().mapToInt(line -> font.width(line.text())).max().orElse(0);
        int height = lines.size() * 10;
        int x = graphics.guiWidth() - width - 12;
        int y = 8;
        graphics.fill(x - 4, y - 4, x + width + 4, y + height + 2, PANEL_BACKGROUND);
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            graphics.text(font, line.text(), x, y + i * 10, line.color(), true);
        }

        if (snapshot.biteReady()) {
            String bite = "BITE!";
            int centerX = graphics.guiWidth() / 2;
            graphics.centeredText(font, bite, centerX, graphics.guiHeight() / 2 - 42, READY);
        }
    }

    private record Line(String text, int color) {
    }
}
