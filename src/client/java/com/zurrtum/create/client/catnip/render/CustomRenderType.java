package com.zurrtum.create.client.catnip.render;

import net.minecraft.client.renderer.rendertype.RenderType;

public interface CustomRenderType {
    boolean create$isPriority();

    void create$markPriority();

    static RenderType markPriority(RenderType type) {
        ((CustomRenderType) type).create$markPriority();
        return type;
    }
}
