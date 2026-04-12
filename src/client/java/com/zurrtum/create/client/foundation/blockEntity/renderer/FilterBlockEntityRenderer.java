package com.zurrtum.create.client.foundation.blockEntity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer.FilterRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.FilterBlockEntityRenderer.FilterBlockEntityRenderState;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public class FilterBlockEntityRenderer implements BlockEntityRenderer<SmartBlockEntity, FilterBlockEntityRenderState> {
    protected final ItemModelResolver itemModelManager;

    public FilterBlockEntityRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public FilterBlockEntityRenderState createRenderState() {
        return new FilterBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(
        SmartBlockEntity be,
        FilterBlockEntityRenderState state,
        float partialTicks,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay breakProgress
    ) {
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
            state.lightCoords = SmartBlockEntityRenderer.getLightCoords(be.getLevel(), blockPos);
            state.blockEntityType = be.getType();
        }
    }

    @Override
    public void submit(
        FilterBlockEntityRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState camera
    ) {
        state.filter.submit(state.blockState, queue, matrices, state.lightCoords);
    }

    public static class FilterBlockEntityRenderState extends BlockEntityRenderState {
        public @UnknownNullability FilterRenderState filter;
    }
}
