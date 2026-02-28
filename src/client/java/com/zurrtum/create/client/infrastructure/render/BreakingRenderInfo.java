package com.zurrtum.create.client.infrastructure.render;

import net.minecraft.client.renderer.block.BlockAndTintGetter;

public interface BreakingRenderInfo {
    void create$setRenderLevel(BlockAndTintGetter level);

    void create$clearRenderLevel();
}
