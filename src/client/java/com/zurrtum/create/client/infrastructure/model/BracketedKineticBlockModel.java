package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.flywheel.lib.model.baked.VirtualBlockGetter;
import com.zurrtum.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BracketedKineticBlockModel extends WrapperBlockStateModel {
    public BracketedKineticBlockModel(BlockState state, UnbakedRoot unbaked) {
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
        BracketedBlockEntityBehaviour attachmentBehaviour = BlockEntityBehaviour.get(
            world,
            pos,
            BracketedBlockEntityBehaviour.TYPE
        );
        if (attachmentBehaviour == null) {
            addVirtualParts(world, random, parts);
            return;
        }
        BlockState bracket = attachmentBehaviour.getBracket();
        if (bracket == null) {
            addVirtualParts(world, random, parts);
            return;
        }
        Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(bracket).collectParts(random, parts);
    }

    private void addVirtualParts(BlockAndTintGetter world, RandomSource random, List<BlockStateModelPart> parts) {
        if (world instanceof VirtualBlockGetter) {
            model.collectParts(random, parts);
        }
    }
}
