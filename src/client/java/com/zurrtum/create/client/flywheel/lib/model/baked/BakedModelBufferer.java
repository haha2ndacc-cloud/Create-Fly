package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.flywheel.lib.model.SimpleModel;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import com.zurrtum.create.foundation.block.LightControlBlock;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

final class BakedModelBufferer {
    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(
        ThreadLocalObjects::new);

    private BakedModelBufferer() {
    }

    private static boolean isDark(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof LightControlBlock block) {
            return block.getLuminance(level, pos) == 0;
        }
        return state.getLightEmission() == 0;
    }

    public static SimpleModel bufferModel(
        SimpleModelWrapper model,
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
        emitters.prepare(blockMaterialFunction);
        emitters.prepareForModelLayer(Minecraft.useAmbientOcclusion() && model.useAmbientOcclusion() && isDark(
            level,
            pos,
            state
        ));
        ModelBlockRenderer blockRenderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
        poseStack.pushPose();
        blockRenderer.tesselateBlock(
            level,
            List.of(model),
            state,
            pos,
            poseStack,
            emitters,
            false,
            OverlayTexture.NO_OVERLAY
        );
        poseStack.popPose();
        return emitters.end();
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
        RandomSource random = objects.random;
        random.setSeed(state.getSeed(pos));
        List<BlockModelPart> parts = model.collectParts(random);
        int size = parts.size();
        if (size == 0) {
            return new SimpleModel(List.of());
        }
        if (poseStack == null) {
            poseStack = objects.identityPoseStack;
        }
        VanillinMeshEmitterManager emitters = objects.emitters;
        emitters.prepare(blockMaterialFunction);
        emitters.prepareForModelLayer(Minecraft.useAmbientOcclusion() && parts.getFirst()
            .useAmbientOcclusion() && isDark(level, pos, state));
        ModelBlockRenderer blockRenderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
        poseStack.pushPose();
        blockRenderer.tesselateBlock(level, parts, state, pos, poseStack, emitters, false, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
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
        RandomSource random = objects.random;
        VanillinMeshEmitterManager emitters = objects.emitters;
        TransformingVertexConsumer transformingWrapper = objects.transformingWrapper;

        emitters.prepare(blockMaterialFunction);

        BlockRenderDispatcher renderDispatcher = Minecraft.getInstance().getBlockRenderer();

        ModelBlockRenderer blockRenderer = renderDispatcher.getModelRenderer();
        ModelBlockRenderer.enableCaching();

        boolean aoEnabled = Minecraft.useAmbientOcclusion();

        while (posIterator.hasNext()) {
            BlockPos pos = posIterator.next();
            BlockState state = level.getBlockState(pos);

            emitters.prepareForBlock();

            if (renderFluids) {
                FluidState fluidState = state.getFluidState();
                if (!fluidState.isEmpty()) {
                    ChunkSectionLayer renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);

                    BufferBuilder bufferBuilder = emitters.getBuffer(renderType, true, false);

                    if (bufferBuilder != null) {
                        transformingWrapper.prepare(bufferBuilder, poseStack);

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
            }

            if (state.getRenderShape() == RenderShape.MODEL) {
                BlockStateModel model = renderDispatcher.getBlockModel(state);
                random.setSeed(state.getSeed(pos));
                List<BlockModelPart> parts = new ObjectArrayList<>();
                if (WrapperBlockStateModel.unwrapCompat(model) instanceof WrapperBlockStateModel wrapper) {
                    wrapper.addPartsWithInfo(level, pos, state, random, parts);
                } else {
                    model.collectParts(random, parts);
                }
                if (!parts.isEmpty()) {
                    emitters.prepareForModelLayer(aoEnabled && parts.getFirst().useAmbientOcclusion());
                    poseStack.pushPose();
                    poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                    blockRenderer.tesselateBlock(
                        level,
                        parts,
                        state,
                        pos,
                        poseStack,
                        emitters,
                        true,
                        OverlayTexture.NO_OVERLAY
                    );
                    poseStack.popPose();
                }
            }
        }

        ModelBlockRenderer.clearCache();
        transformingWrapper.clear();
        return emitters.end();
    }

    private static class ThreadLocalObjects {
        public final PoseStack identityPoseStack = new PoseStack();
        public final RandomSource random = RandomSource.createNewThreadLocalInstance();

        public final VanillinMeshEmitterManager emitters = new VanillinMeshEmitterManager();
        public final TransformingVertexConsumer transformingWrapper = new TransformingVertexConsumer();
    }
}
