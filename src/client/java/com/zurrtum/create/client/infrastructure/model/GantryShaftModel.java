package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.client.flywheel.lib.model.baked.VirtualBlockGetter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class GantryShaftModel extends WrapperBlockStateModel {
    public GantryShaftModel(BlockState state, UnbakedRoot unbaked) {
        super(state, unbaked);
    }

    @Override
    public void addPartsWithInfo(
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState state,
        RandomSource random,
        List<BlockStateModelPart> parts
    ) {
        if (world instanceof VirtualBlockGetter) {
            model.collectParts(random, parts);
        }
    }
}
