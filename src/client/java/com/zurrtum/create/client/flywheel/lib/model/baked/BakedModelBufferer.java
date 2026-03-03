package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.flywheel.lib.model.SimpleModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.*;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;

final class BakedModelBufferer {
    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(
        ThreadLocalObjects::new);

    private BakedModelBufferer() {
    }

    public static SimpleModel bufferModel(
        BlockStateModel model,
        BlockPos pos,
        BlockAndTintGetter level,
        BlockState state,
        @Nullable PoseStack poseStack,
        BlockMaterialFunction blockMaterialFunction
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }
        VanillinMeshEmitterManager emitters = objects.emitters;
        emitters.prepare(blockMaterialFunction, poseStack);
        Minecraft minecraft = Minecraft.getInstance();
        boolean ambientOcclusion = minecraft.options.ambientOcclusion().get();
        ModelBlockRenderer blockRenderer = new ModelBlockRenderer(ambientOcclusion, false, minecraft.getBlockColors());
        blockRenderer.tesselateBlock(emitters, 0, 0, 0, level, pos, state, model, state.getSeed(pos));
        return emitters.end();
    }

    public static SimpleModel bufferBlocks(
        Iterator<BlockPos> posIterator,
        BlockAndTintGetter level,
        @Nullable PoseStack poseStack,
        boolean renderFluids,
        BlockMaterialFunction blockMaterialFunction
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }
        VanillinMeshEmitterManager emitters = objects.emitters;
        TransformingVertexConsumer transformingWrapper = objects.transformingWrapper;

        emitters.prepare(blockMaterialFunction, poseStack);

        BlockRenderDispatcher renderDispatcher = Minecraft.getInstance().getBlockRenderer();
        LiquidBlockRenderer liquidRenderer = renderDispatcher.getLiquidRenderer();

        Minecraft minecraft = Minecraft.getInstance();
        boolean ambientOcclusion = minecraft.options.ambientOcclusion().get();
        ModelBlockRenderer blockRenderer = new ModelBlockRenderer(ambientOcclusion, true, minecraft.getBlockColors());
        BlockModelLighter.enableCaching();

        while (posIterator.hasNext()) {
            BlockPos pos = posIterator.next();
            BlockState state = level.getBlockState(pos);

            emitters.prepareForBlock();

            if (renderFluids) {
                FluidState fluidState = state.getFluidState();
                if (!fluidState.isEmpty()) {
                    ChunkSectionLayer renderType = liquidRenderer.getRenderLayer(fluidState);

                    BufferBuilder bufferBuilder = emitters.getBuffer(renderType, true, false);

                    if (bufferBuilder != null) {
                        transformingWrapper.prepare(bufferBuilder, poseStack);

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
            }

            if (state.getRenderShape() == RenderShape.MODEL) {
                blockRenderer.tesselateBlock(
                    emitters,
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
        return emitters.end();
    }

    private static class ThreadLocalObjects {
        public final PoseStack identityPoseStack = new PoseStack();
        public final VanillinMeshEmitterManager emitters = new VanillinMeshEmitterManager();
        public final TransformingVertexConsumer transformingWrapper = new TransformingVertexConsumer();
    }
}
