package com.zurrtum.create.client.catnip.impl.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.client.render.model.ShadeSeparatedBufferSource;
import com.zurrtum.create.client.catnip.client.render.model.ShadeSeparatedResultConsumer;
import com.zurrtum.create.client.catnip.impl.client.render.TransformingVertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.*;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;

// Modified from https://github.com/Engine-Room/Flywheel/blob/2f67f54c8898d91a48126c3c753eefa6cd224f84/forge/src/lib/java/dev/engine_room/flywheel/lib/model/baked/BakedModelBufferer.java
public final class BakedModelBuffererImpl {
    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(
        ThreadLocalObjects::new);

    private BakedModelBuffererImpl() {
    }


    public static void bufferModel(
        BlockStateModel model,
        BlockPos pos,
        BlockAndTintGetter level,
        BlockState state,
        @Nullable PoseStack poseStack,
        ShadeSeparatedBufferSource bufferSource
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }
        UniversalMeshEmitter universalEmitter = objects.universalEmitter;
        Minecraft minecraft = Minecraft.getInstance();
        boolean ambientOcclusion = minecraft.options.ambientOcclusion().get();
        ModelBlockRenderer blockRenderer = new ModelBlockRenderer(ambientOcclusion, false, minecraft.getBlockColors());
        universalEmitter.prepare(bufferSource, poseStack);
        blockRenderer.tesselateBlock(universalEmitter, 0, 0, 0, level, pos, state, model, state.getSeed(pos));
        universalEmitter.clear();
    }

    public static void bufferModel(
        BlockStateModel model,
        BlockPos pos,
        BlockAndTintGetter level,
        BlockState state,
        @Nullable PoseStack poseStack,
        ShadeSeparatedResultConsumer resultConsumer
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        DefaultShadeSeparatedBufferSource bufferSource = objects.defaultBufferSource;
        bufferSource.prepare(resultConsumer);
        bufferModel(model, pos, level, state, poseStack, bufferSource);
        bufferSource.end();
    }

    public static void bufferBlocks(
        Iterator<BlockPos> posIterator,
        BlockAndTintGetter level,
        @Nullable PoseStack poseStack,
        boolean renderFluids,
        ShadeSeparatedBufferSource bufferSource
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }
        UniversalMeshEmitter universalEmitter = objects.universalEmitter;
        TransformingVertexConsumer transformingWrapper = objects.transformingWrapper;

        universalEmitter.prepare(bufferSource, poseStack);

        BlockRenderDispatcher renderDispatcher = Minecraft.getInstance().getBlockRenderer();
        LiquidBlockRenderer liquidRenderer = renderDispatcher.getLiquidRenderer();

        Minecraft minecraft = Minecraft.getInstance();
        boolean ambientOcclusion = minecraft.options.ambientOcclusion().get();
        ModelBlockRenderer blockRenderer = new ModelBlockRenderer(ambientOcclusion, true, minecraft.getBlockColors());
        BlockModelLighter.enableCaching();

        while (posIterator.hasNext()) {
            BlockPos pos = posIterator.next();
            BlockState state = level.getBlockState(pos);

            if (renderFluids) {
                FluidState fluidState = state.getFluidState();

                if (!fluidState.isEmpty()) {
                    ChunkSectionLayer renderType = liquidRenderer.getRenderLayer(fluidState);

                    transformingWrapper.prepare(bufferSource.getBuffer(renderType, true), poseStack);

                    poseStack.pushPose();
                    poseStack.translate(
                        pos.getX() - (pos.getX() & 0xF),
                        pos.getY() - (pos.getY() & 0xF),
                        pos.getZ() - (pos.getZ() & 0xF)
                    );
                    liquidRenderer.tesselate(level, pos, transformingWrapper, state, fluidState);
                    poseStack.popPose();
                }
            }

            if (state.getRenderShape() == RenderShape.MODEL) {
                blockRenderer.tesselateBlock(
                    universalEmitter,
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    level,
                    pos,
                    state,
                    renderDispatcher.getBlockModel(state),
                    state.getSeed(pos)
                );
            }
        }

        BlockModelLighter.clearCache();
        transformingWrapper.clear();
        universalEmitter.clear();
    }

    public static void bufferBlocks(
        Iterator<BlockPos> posIterator,
        BlockAndTintGetter level,
        @Nullable PoseStack poseStack,
        boolean renderFluids,
        ShadeSeparatedResultConsumer resultConsumer
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        DefaultShadeSeparatedBufferSource bufferSource = objects.defaultBufferSource;
        bufferSource.prepare(resultConsumer);
        bufferBlocks(posIterator, level, poseStack, renderFluids, bufferSource);
        bufferSource.end();
    }

    private static class ThreadLocalObjects {
        public final PoseStack identityPoseStack = new PoseStack();

        public final DefaultShadeSeparatedBufferSource defaultBufferSource = new DefaultShadeSeparatedBufferSource();
        public final UniversalMeshEmitter universalEmitter = new UniversalMeshEmitter();
        public final TransformingVertexConsumer transformingWrapper = new TransformingVertexConsumer();
    }
}
