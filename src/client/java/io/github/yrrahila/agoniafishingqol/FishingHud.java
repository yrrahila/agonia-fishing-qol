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
    private static final int READY = 0xFFFFD34E;
    private static final float WAIT_SCALE = 2.0F;
    private static final float BITE_SCALE = 3.0F;

    private FishingHud() {
    }

    public static void render(GuiGraphicsExtractor graphics, FishingSnapshot snapshot) {
        Minecraft client = Minecraft.getInstance();
        Font font = client.font;
        List<Line> lines = new ArrayList<>();
        int stateColor = switch (snapshot.status()) {
            case NOT_CAST -> MUTED;
            case FISH_APPROACHING -> READY;
            case BITE_READY -> GOOD;
            case WAITING -> TEXT;
        };
        lines.add(new Line("Fishing: " + snapshot.status().label(), stateColor));

        if (snapshot.status() != FishingSnapshot.BobberStatus.NOT_CAST) {
            lines.add(new Line(snapshot.estimatedBite(), snapshot.biteReady() ? GOOD : TEXT));
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
            drawAlert(graphics, font, "BITE!", GOOD, BITE_SCALE);
        } else if (snapshot.status() == FishingSnapshot.BobberStatus.FISH_APPROACHING) {
            drawAlert(graphics, font, "WAIT...", READY, WAIT_SCALE);
        }
    }

    private static void drawAlert(GuiGraphicsExtractor graphics, Font font, String text, int color, float scale) {
        int centerX = Math.round((graphics.guiWidth() / 2.0F) / scale);
        int alertY = Math.round((graphics.guiHeight() / 2.0F - 48.0F) / scale);
        graphics.pose().pushMatrix();
        graphics.pose().scale(scale, scale);
        graphics.centeredText(font, text, centerX, alertY, color);
        graphics.pose().popMatrix();
    }

    private record Line(String text, int color) {
    }
}
