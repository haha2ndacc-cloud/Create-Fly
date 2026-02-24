package com.zurrtum.create.client.foundation.utility.worldWrappers;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

public class WrappedBlockAndTintGetter implements BlockAndTintGetter {
    protected final BlockAndTintGetter wrapped;

    public WrappedBlockAndTintGetter(BlockAndTintGetter wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        return wrapped.getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return wrapped.getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return wrapped.getFluidState(pos);
    }

    @Override
    public int getHeight() {
        return wrapped.getHeight();
    }

    @Override
    public int getMinY() {
        return wrapped.getMinY();
    }

    @Override
    public CardinalLighting cardinalLighting() {
        return wrapped.cardinalLighting();
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return wrapped.getLightEngine();
    }

    @Override
    public int getBlockTint(BlockPos pBlockPos, ColorResolver pColorResolver) {
        return wrapped.getBlockTint(pBlockPos, pColorResolver);
    }

}
