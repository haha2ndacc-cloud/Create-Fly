package com.zurrtum.create.client.catnip.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.impl.client.render.model.BakedModelBuffererImpl;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;

public class BakedModelBufferer {
    private BakedModelBufferer() {
    }

    public static void bufferModel(
        BlockStateModel model,
        BlockPos pos,
        BlockAndTintGetter level,
        BlockState state,
        @Nullable PoseStack poseStack,
        ShadeSeparatedBufferSource bufferSource
    ) {
        BakedModelBuffererImpl.bufferModel(model, pos, level, state, poseStack, bufferSource);
    }

    public static void bufferModel(
        BlockStateModel model,
        BlockPos pos,
        BlockAndTintGetter level,
        BlockState state,
        @Nullable PoseStack poseStack,
        ShadeSeparatedResultConsumer resultConsumer
    ) {
        BakedModelBuffererImpl.bufferModel(model, pos, level, state, poseStack, resultConsumer);
    }

    public static void bufferBlocks(
        Iterator<BlockPos> posIterator,
        BlockAndTintGetter level,
        @Nullable PoseStack poseStack,
        boolean renderFluids,
        ShadeSeparatedBufferSource bufferSource
    ) {
        BakedModelBuffererImpl.bufferBlocks(posIterator, level, poseStack, renderFluids, bufferSource);
    }

    public static void bufferBlocks(
        Iterator<BlockPos> posIterator,
        BlockAndTintGetter level,
        @Nullable PoseStack poseStack,
        boolean renderFluids,
        ShadeSeparatedResultConsumer resultConsumer
    ) {
        BakedModelBuffererImpl.bufferBlocks(posIterator, level, poseStack, renderFluids, resultConsumer);
    }
}
