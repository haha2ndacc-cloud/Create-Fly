package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllCTBehaviours;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.content.decoration.girder.GirderBlock;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class ConnectedGirderModel extends CTModel {
    public ConnectedGirderModel(BlockState state, UnbakedRoot unbaked) {
        super(state, unbaked, AllCTBehaviours.METAL_GIRDER);
    }

    @Override
    public void addPartsWithInfo(
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState state,
        RandomSource random,
        List<BlockStateModelPart> parts
    ) {
        super.addPartsWithInfo(world, pos, state, random, parts);
        for (Direction direction : Iterate.horizontalDirections) {
            if (GirderBlock.isConnected(world, pos, state, direction)) {
                AllPartialModels.METAL_GIRDER_BRACKETS.get(direction).get().collectParts(random, parts);
            }
        }
    }
}
