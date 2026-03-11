package com.zurrtum.create.client.compat.jei.renderer;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record IconRenderer(ItemStack icon) implements IDrawable {
    public IconRenderer(Item icon) {
        this(new ItemStack(icon));
    }

    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    public void draw(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.item(icon, x, y);
    }
}
