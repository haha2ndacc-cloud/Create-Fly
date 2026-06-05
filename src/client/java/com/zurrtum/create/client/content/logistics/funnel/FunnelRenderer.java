package com.zurrtum.create.client.content.logistics.funnel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.logistics.FlapStuffs;
import com.zurrtum.create.client.content.logistics.FlapStuffs.FlapsRenderState;
import com.zurrtum.create.client.content.logistics.funnel.FunnelRenderer.FunnelRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer.FilterRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.logistics.funnel.AbstractFunnelBlock;
import com.zurrtum.create.content.logistics.funnel.FunnelBlock;
import com.zurrtum.create.content.logistics.funnel.FunnelBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FunnelRenderer implements BlockEntityRenderer<FunnelBlockEntity, FunnelRenderState> {
    protected final ItemModelResolver itemModelManager;

    public FunnelRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public FunnelRenderState createRenderState() {
        return new FunnelRenderState();
    }

    @Override
    public void extractRenderState(
        FunnelBlockEntity be,
        FunnelRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = be.getLevel();
        if (!be.hasFlap() || VisualizationManager.supportsVisualization(level)) {
            BlockPos blockPos = be.getBlockPos();
            BlockState blockState = be.getBlockState();
            state.filter = FilteringRenderer.getFilterRenderState(
                be,
                blockState,
                itemModelManager,
                be.isVirtual() ? -1 : cameraPos.distanceToSqr(VecHelper.getCenterOf(blockPos))
            );
            if (state.filter != null) {
                state.blockPos = blockPos;
                state.blockState = blockState;
                state.blockEntityType = be.getType();
                state.lightCoords = SmartBlockEntityRenderer.getLightCoords(level, state.blockPos);
            }
            return;
        }
        SmartBlockEntityRenderer.extractBase(level, be, state, crumblingOverlay);
        AbstractFunnelBlock block = (AbstractFunnelBlock) state.blockState.getBlock();
        PartialModel partialModel =
            block instanceof FunnelBlock ? AllPartialModels.FUNNEL_FLAP : AllPartialModels.BELT_FUNNEL_FLAP;
        SuperByteBufferRenderState flapBuffer = CachedBuffers.partial(partialModel, state.blockState)
            .cardinalLighting(level).light(state.lightCoords).extractRenderState();
        state.flap = FlapStuffs.getFlapsRenderState(
            flapBuffer,
            FlapStuffs.FUNNEL_PIVOT,
            block.getFacing(state.blockState),
            be.flap.getValue(tickProgress),
            -be.getFlapOffset()
        );
        state.filter = FilteringRenderer.getFilterRenderState(
            be,
            state.blockState,
            itemModelManager,
            be.isVirtual() ? -1 : cameraPos.distanceToSqr(VecHelper.getCenterOf(state.blockPos))
        );
    }

    @Override
    public void submit(
        FunnelRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.filter != null) {
            state.filter.submit(state.blockState, queue, matrices, state.lightCoords);
        }
        if (state.flap != null) {
            state.flap.submit(matrices, queue);
        }
    }

    public static class FunnelRenderState extends BlockEntityRenderState {
        public @Nullable FilterRenderState filter;
        public @Nullable FlapsRenderState flap;
    }
}
