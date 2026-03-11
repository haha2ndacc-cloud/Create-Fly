package com.zurrtum.create.client.catnip.gui.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.world.level.block.state.BlockState;

public class BlockTransformRenderKey {
    public BlockState state;
    public BlockStateModel model;
    public boolean dirty;
    public int padding;
    public float size, xRot, yRot, zRot;

    public BlockTransformRenderKey(BlockState state) {
        this.state = state;
        model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
    }

    public void update(float scale, int padding, float xRot, float yRot, float zRot) {
        float size = scale * 16 + padding;
        if (size != this.size || xRot != this.xRot || yRot != this.yRot || zRot != this.zRot) {
            dirty = true;
            this.size = size;
            this.padding = padding;
            this.xRot = xRot;
            this.yRot = yRot;
            this.zRot = zRot;
        }
    }
}
