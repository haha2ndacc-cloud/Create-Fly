package com.zurrtum.create.infrastructure.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.WaterFluid;
import org.jspecify.annotations.Nullable;

public abstract class FlowableFluid extends WaterFluid {
    @Override
    public void animateTick(Level world, BlockPos pos, FluidState state, RandomSource random) {
    }

    @Override
    @Nullable
    public ParticleOptions getDripParticle() {
        return null;
    }

    @Override
    protected void spreadTo(
        LevelAccessor world,
        BlockPos pos,
        BlockState state,
        Direction direction,
        FluidState fluidState
    ) {
        if (state.getBlock() instanceof LiquidBlockContainer fluidFillable) {
            fluidFillable.placeLiquid(world, pos, state, fluidState);
        } else if (state.getFluidState().isEmpty()) {
            world.setBlock(pos, fluidState.createLegacyBlock(), Block.UPDATE_ALL);
        }
    }

    @Override
    public int getDropOff(LevelReader world) {
        return 2;
    }

    @Override
    public int getSlopeFindDistance(LevelReader world) {
        return 3;
    }

    @Override
    public int getTickDelay(LevelReader world) {
        return 25;
    }

    @Override
    protected boolean canConvertToSource(ServerLevel world) {
        return false;
    }

    @Override
    public boolean canBeReplacedWith(
        FluidState state,
        BlockGetter world,
        BlockPos pos,
        Fluid fluid,
        Direction direction
    ) {
        return false;
    }
}
