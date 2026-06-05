package com.zurrtum.create.client.catnip.ghostblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class GhostBlockParams {

    protected final BlockState state;
    protected BlockPos pos;
    protected Supplier<Float> alphaSupplier;

    private GhostBlockParams(BlockState state) {
        this.state = state;
        pos = BlockPos.ZERO;
        alphaSupplier = () -> 1.0f;
    }

    public static GhostBlockParams of(BlockState state) {
        return new GhostBlockParams(state);
    }

    public static GhostBlockParams of(Block block) {
        return of(block.defaultBlockState());
    }

    public GhostBlockParams at(BlockPos pos) {
        this.pos = pos;
        return this;
    }

    public GhostBlockParams at(int x, int y, int z) {
        return at(new BlockPos(x, y, z));
    }

    public GhostBlockParams alpha(Supplier<Float> alphaSupplier) {
        this.alphaSupplier = alphaSupplier;
        return this;
    }

    public GhostBlockParams alpha(float alpha) {
        return alpha(() -> alpha);
    }

    public GhostBlockParams breathingAlpha() {
        return alpha(() -> (float) GhostBlocks.getBreathingAlpha());
    }
}
