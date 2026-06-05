package com.zurrtum.create.client.catnip.gui.element;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public abstract class AbstractRenderElement implements RenderElement {

    public static RenderElement EMPTY = new AbstractRenderElement() {
        @Override
        public void render(GuiGraphicsExtractor graphics) {
        }
    };

    protected int width = 16, height = 16;
    protected float x, y, z;
    protected float alpha = 1.0f;

    @Override
    public <T extends RenderElement> T at(float x, float y) {
        this.x = x;
        this.y = y;
        //noinspection unchecked
        return (T) this;
    }

    @Override
    public <T extends RenderElement> T at(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        //noinspection unchecked
        return (T) this;
    }

    @Override
    public <T extends RenderElement> T withBounds(int width, int height) {
        this.width = width;
        this.height = height;
        //noinspection unchecked
        return (T) this;
    }

    @Override
    public <T extends RenderElement> T withAlpha(float alpha) {
        this.alpha = alpha;
        //noinspection unchecked
        return (T) this;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public float getZ() {
        return z;
    }

    public static class SimpleRenderElement extends AbstractRenderElement {

        private final ScreenElement renderable;

        public SimpleRenderElement(ScreenElement renderable) {
            this.renderable = renderable;
        }

        @Override
        public void render(GuiGraphicsExtractor graphics) {
            renderable.render(graphics, (int) x, (int) y);
        }
    }
}
