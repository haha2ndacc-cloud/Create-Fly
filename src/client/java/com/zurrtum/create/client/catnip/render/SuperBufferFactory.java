package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.client.flywheel.lib.model.baked.EmptyVirtualBlockGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class SuperBufferFactory {
    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(
        ThreadLocalObjects::new);
    private static final SuperBufferFactory INSTANCE = new SuperBufferFactory();

    public static SuperBufferFactory getInstance() {
        return INSTANCE;
    }

    public SuperByteBuffer createForBlock(BlockState renderedState) {
        return createForBlock(
            THREAD_LOCAL_OBJECTS.get().sbbBuilder,
            Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(renderedState),
            renderedState
        );
    }

    public SuperByteBuffer createForBlock(BlockStateModel model, BlockState state) {
        return createForBlock(THREAD_LOCAL_OBJECTS.get().sbbBuilder, model, state);
    }

    public SuperByteBuffer createForBlock(BlockStateModel model, BlockState state, Pose pose) {
        return createForBlock(THREAD_LOCAL_OBJECTS.get().transformSbbBuilder.wrap(pose), model, state);
    }

    private static SuperByteBuffer createForBlock(
        EntityBlockSbbBuilder sbbBuilder,
        BlockStateModel model,
        BlockState state
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean ambientOcclusion = minecraft.options.ambientOcclusion().get();
        ModelBlockRenderer blockRenderer = new ModelBlockRenderer(ambientOcclusion, false, minecraft.getBlockColors());
        blockRenderer.tesselateBlock(
            sbbBuilder,
            0,
            0,
            0,
            EmptyVirtualBlockGetter.FULL_DARK,
            BlockPos.ZERO,
            state,
            model,
            state.getSeed(BlockPos.ZERO)
        );
        return sbbBuilder.build();
    }

    private static class ThreadLocalObjects {
        public final EntityBlockSbbBuilder sbbBuilder = new EntityBlockSbbBuilder();
        public final EntityBlockTransformSbbBuilder transformSbbBuilder = new EntityBlockTransformSbbBuilder();
    }
}
