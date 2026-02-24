package com.zurrtum.create.client.catnip.impl.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.client.render.model.ShadeSeparatedBufferSource;
import com.zurrtum.create.client.catnip.client.render.model.ShadeSeparatedResultConsumer;
import com.zurrtum.create.client.catnip.impl.client.render.TransformingVertexConsumer;
import com.zurrtum.create.client.infrastructure.model.CopycatModel;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

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
        RandomSource random = objects.random;
        random.setSeed(state.getSeed(pos));
        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }
        List<BlockModelPart> parts = new ObjectArrayList<>();
        if (model instanceof CopycatModel copycatModel) {
            copycatModel.addPartsWithInfo(level, pos, state, random, parts);
        } else {
            model.collectParts(random, parts);
        }
        bufferModel(parts, pos, level, state, poseStack, bufferSource, objects.universalEmitter);
    }

    private static void bufferModel(
        List<BlockModelPart> parts,
        BlockPos pos,
        BlockAndTintGetter level,
        BlockState state,
        PoseStack poseStack,
        ShadeSeparatedBufferSource bufferSource,
        UniversalMeshEmitter universalEmitter
    ) {
        int size = parts.size();
        if (size == 0) {
            return;
        }
        ModelBlockRenderer blockRenderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
        universalEmitter.prepare(bufferSource);
        poseStack.pushPose();
        blockRenderer.tesselateBlock(
            level,
            parts,
            state,
            pos,
            poseStack,
            universalEmitter,
            false,
            OverlayTexture.NO_OVERLAY
        );
        poseStack.popPose();
        universalEmitter.clear();
    }

    public static void bufferModel(
        List<BlockModelPart> parts,
        BlockPos pos,
        BlockAndTintGetter level,
        BlockState state,
        @Nullable PoseStack poseStack,
        ShadeSeparatedResultConsumer resultConsumer
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        DefaultShadeSeparatedBufferSource bufferSource = objects.defaultBufferSource;
        bufferSource.prepare(resultConsumer);
        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }
        bufferModel(parts, pos, level, state, poseStack, bufferSource, objects.universalEmitter);
        bufferSource.end();
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
        RandomSource random = objects.random;
        UniversalMeshEmitter universalEmitter = objects.universalEmitter;
        TransformingVertexConsumer transformingWrapper = objects.transformingWrapper;

        BlockRenderDispatcher renderDispatcher = Minecraft.getInstance().getBlockRenderer();
        LiquidBlockRenderer liquidRenderer = renderDispatcher.getLiquidRenderer();

        ModelBlockRenderer blockRenderer = renderDispatcher.getModelRenderer();
        ModelBlockRenderer.enableCaching();

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
                    renderDispatcher.renderLiquid(pos, level, transformingWrapper, state, fluidState);
                    poseStack.popPose();
                }
            }

            if (state.getRenderShape() == RenderShape.MODEL) {
                BlockStateModel model = renderDispatcher.getBlockModel(state);
                random.setSeed(state.getSeed(pos));
                universalEmitter.prepare(bufferSource);
                poseStack.pushPose();
                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                List<BlockModelPart> parts = new ObjectArrayList<>();
                if (WrapperBlockStateModel.unwrapCompat(model) instanceof WrapperBlockStateModel wrapper) {
                    wrapper.addPartsWithInfo(level, pos, state, random, parts);
                } else {
                    model.collectParts(random, parts);
                }
                blockRenderer.tesselateBlock(
                    level,
                    parts,
                    state,
                    pos,
                    poseStack,
                    universalEmitter,
                    true,
                    OverlayTexture.NO_OVERLAY
                );
                poseStack.popPose();
            }
        }

        ModelBlockRenderer.clearCache();
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
        public final RandomSource random = RandomSource.createNewThreadLocalInstance();

        public final DefaultShadeSeparatedBufferSource defaultBufferSource = new DefaultShadeSeparatedBufferSource();
        public final UniversalMeshEmitter universalEmitter = new UniversalMeshEmitter();
        public final TransformingVertexConsumer transformingWrapper = new TransformingVertexConsumer();
    }
}
