package dev.lumina.engine.fabric.v1_21_11;

import dev.lumina.engine.common.HudPosition;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

final class PerformanceHud {
    private PerformanceHud() {}

    static void initialize() {
        HudElementRegistry.addLast(Identifier.of(LuminaEngineClient.MOD_ID, "performance_hud"),
            (context, tickCounter) -> render(context));
    }

    private static void render(DrawContext context) {
        if (!FrameTelemetryRuntime.hudEnabled()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        var metrics = FrameTelemetryRuntime.latest();
        String action = FrameTelemetryRuntime.actionableRecommendation()
            .map(value -> value.result().recommendation().name()).orElse("HOLD");
        List<String> lines = List.of(
            String.format(Locale.ROOT, "Lumina %.1f FPS", metrics.averageFps()),
            String.format(Locale.ROOT, "1%% low %.1f / target %d", metrics.onePercentLowFps(), metrics.targetFps()),
            metrics.targetStatus().name() + " / " + action
        );
        int width = lines.stream().mapToInt(client.textRenderer::getWidth).max().orElse(0) + 8;
        int height = lines.size() * 10 + 6;
        int[] position = position(FrameTelemetryRuntime.hudPosition(), client.getWindow().getScaledWidth(),
            client.getWindow().getScaledHeight(), width, height);
        context.fill(position[0], position[1], position[0] + width, position[1] + height, 0xA0000000);
        for (int i = 0; i < lines.size(); i++) context.drawTextWithShadow(client.textRenderer, lines.get(i), position[0] + 4, position[1] + 3 + i * 10, 0xFFFFFF);
    }

    private static int[] position(HudPosition position, int screenWidth, int screenHeight, int width, int height) {
        int x = position == HudPosition.TOP_RIGHT || position == HudPosition.BOTTOM_RIGHT ? screenWidth - width - 6 : 6;
        int y = position == HudPosition.BOTTOM_LEFT || position == HudPosition.BOTTOM_RIGHT ? screenHeight - height - 6 : 6;
        return new int[] {x, y};
    }
}
