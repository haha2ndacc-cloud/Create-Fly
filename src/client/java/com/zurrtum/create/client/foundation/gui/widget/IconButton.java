package com.zurrtum.create.client.foundation.gui.widget;

import com.zurrtum.create.client.catnip.gui.element.ScreenElement;
import com.zurrtum.create.client.catnip.gui.widget.AbstractSimiWidget;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

public class IconButton extends AbstractSimiWidget {

    protected ScreenElement icon;

    public boolean green;
    private boolean down;

    public IconButton(int x, int y, ScreenElement icon) {
        this(x, y, 18, 18, icon);
    }

    public IconButton(int x, int y, int w, int h, ScreenElement icon) {
        super(x, y, w, h);
        this.icon = icon;
    }

    @Override
    public void onClick(MouseButtonEvent click, boolean doubled) {
        super.onClick(click, doubled);
        down = true;
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        down = false;
    }

    @Override
    public void doRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;
            AllGuiTextures button;
            if (!active) {
                button = AllGuiTextures.BUTTON_DISABLED;
            } else if (isHovered) {
                button = down ? AllGuiTextures.BUTTON_DOWN : AllGuiTextures.BUTTON_HOVER;
            } else if (green) {
                button = AllGuiTextures.BUTTON_GREEN;
            } else {
                button = AllGuiTextures.BUTTON;
            }
            drawBg(graphics, button);
            icon.render(graphics, getX() + 1, getY() + 1);
        }
    }

    protected void drawBg(GuiGraphicsExtractor graphics, AllGuiTextures button) {
        graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            button.location,
            getX(),
            getY(),
            button.getStartX(),
            button.getStartY(),
            button.getWidth(),
            button.getHeight(),
            256,
            256
        );
    }

    public void setToolTip(Component text) {
        toolTip.clear();
        toolTip.add(text);
    }

    public void setIcon(ScreenElement icon) {
        this.icon = icon;
    }
}
