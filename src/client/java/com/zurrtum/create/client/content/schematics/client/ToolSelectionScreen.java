package com.zurrtum.create.client.content.schematics.client;

import com.mojang.blaze3d.platform.Window;
import com.zurrtum.create.client.AllKeys;
import com.zurrtum.create.client.content.schematics.client.tools.ToolType;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;

import java.util.List;
import java.util.function.Consumer;

public class ToolSelectionScreen extends Screen {
    private static final int[] TOOLTIP_OFFSET = {38, 50, 60, 72};
    private static final int[] COLORS = {0xEEEEEE, 0xCCDDFF, 0xCCDDFF, 0xCCCCDD};

    public final String scrollToCycle = CreateLang.translateDirect("gui.toolmenu.cycle").getString();
    public final String holdToFocus = "gui.toolmenu.focusKey";

    protected List<ToolType> tools;
    protected Consumer<ToolType> callback;
    public boolean focused;
    private float yOffset;
    protected int selection;
    private boolean initialized;

    protected int w;
    protected int h;

    public ToolSelectionScreen(Minecraft mc, List<ToolType> tools, Consumer<ToolType> callback) {
        super(mc, mc.font, Component.literal("Tool Selection"));
        this.tools = tools;
        this.callback = callback;
        focused = false;
        yOffset = 0;
        selection = 0;
        initialized = false;

        callback.accept(tools.get(selection));

        w = Math.max(tools.size() * 50 + 30, 220);
        h = 30;
    }

    public void setSelectedElement(ToolType tool) {
        if (!tools.contains(tool)) {
            return;
        }
        selection = tools.indexOf(tool);
    }

    public void cycle(int direction) {
        selection += direction < 0 ? 1 : -1;
        selection = (selection + tools.size()) % tools.size();
    }

    private void draw(GuiGraphicsExtractor graphics, float partialTicks) {
        Matrix3x2fStack matrixStack = graphics.pose();
        Window mainWindow = minecraft.getWindow();
        int scaledWidth = mainWindow.getGuiScaledWidth();
        int scaledHeight = mainWindow.getGuiScaledHeight();
        if (!initialized) {
            init(scaledWidth, scaledHeight);
        }

        int x = (scaledWidth - w) / 2 + 15;
        int y = scaledHeight - h - 75;

        matrixStack.pushMatrix();
        matrixStack.translate(0, -yOffset);

        AllGuiTextures gray = AllGuiTextures.HUD_BACKGROUND;

        graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            gray.location,
            x - 15,
            y,
            gray.getStartX(),
            gray.getStartY(),
            w,
            h,
            gray.getWidth(),
            gray.getHeight(),
            focused ? 0xE0FFFFFF : 0x80FFFFFF
        );

        float toolTipAlpha = yOffset / 10;
        List<Component> toolTip = tools.get(selection).getDescription();
        int stringAlphaComponent = (int) (toolTipAlpha * 0xFF) << 24;

        if (toolTipAlpha > 0.25f) {
            graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                gray.location,
                x - 15,
                y + 33,
                gray.getStartX(),
                gray.getStartY(),
                w,
                h + 22,
                gray.getWidth(),
                gray.getHeight(),
                0xB2B2CC | stringAlphaComponent
            );
            for (int i = 0, size = Math.min(toolTip.size(), 4); i < size; i++) {
                graphics.text(
                    font,
                    toolTip.get(i),
                    x - 10,
                    y + TOOLTIP_OFFSET[i],
                    COLORS[i] | stringAlphaComponent,
                    false
                );
            }
        }

        if (tools.size() > 1) {
            String keyName = AllKeys.TOOL_MENU.getTranslatedKeyMessage().getString().toUpperCase();
            if (!focused) {
                graphics.centeredText(
                    font,
                    CreateLang.translateDirect(holdToFocus, keyName),
                    scaledWidth / 2,
                    y - 10,
                    0xFFCCDDFF
                );
            } else {
                graphics.centeredText(font, scrollToCycle, scaledWidth / 2, y - 10, 0xFFCCDDFF);
            }
        } else {
            x += 65;
        }


        for (int i = 0; i < tools.size(); i++) {
            matrixStack.pushMatrix();

            float alpha = focused ? 1 : 0.2f;
            if (i == selection) {
                matrixStack.translate(0, -10);
                graphics.centeredText(
                    font,
                    tools.get(i).getDisplayName().getString(),
                    x + i * 50 + 24,
                    y + 28,
                    0xFFCCDDFF
                );
                alpha = 1;
            }
            int color = (int) (alpha * 0xFF) << 24;
            tools.get(i).getIcon().render(graphics, x + i * 50 + 16, y + 12, color);
            tools.get(i).getIcon().render(graphics, x + i * 50 + 16, y + 11, 0xFFFFFF | color);

            matrixStack.popMatrix();
        }

        matrixStack.popMatrix();
    }

    public void update() {
        if (focused) {
            yOffset += (10 - yOffset) * 0.1f;
        } else {
            yOffset *= 0.9f;
        }
    }

    public void renderPassive(GuiGraphicsExtractor graphics, float partialTicks) {
        draw(graphics, partialTicks);
    }

    @Override
    public void onClose() {
        callback.accept(tools.get(selection));
    }

    @Override
    protected void init() {
        super.init();
        initialized = true;
    }
}
