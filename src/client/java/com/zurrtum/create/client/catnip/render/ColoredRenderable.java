package com.zurrtum.create.client.catnip.render;

import com.zurrtum.create.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface ColoredRenderable {

    void render(GuiGraphicsExtractor graphics, int x, int y, Color c);

}
