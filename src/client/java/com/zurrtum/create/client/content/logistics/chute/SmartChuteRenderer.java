package com.zurrtum.create.client.content.logistics.chute;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.logistics.chute.ChuteRenderer.ChuteItemRenderState;
import com.zurrtum.create.client.content.logistics.chute.SmartChuteRenderer.SmartChuteRenderState;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer.FilterRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.logistics.chute.SmartChuteBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SmartChuteRenderer implements BlockEntityRenderer<SmartChuteBlockEntity, SmartChuteRenderState> {
    protected final ItemModelResolver itemModelManager;

    public SmartChuteRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public SmartChuteRenderState createRenderState() {
        return new SmartChuteRenderState();
    }

    @Override
    public void extractRenderState(
        SmartChuteBlockEntity be,
        SmartChuteRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = be.getLevel();
        BlockPos pos = be.getBlockPos();
        BlockState blockState = be.getBlockState();
        double distance = be.isVirtual() ? -1 : cameraPos.distanceToSqr(VecHelper.getCenterOf(pos));
        state.filter = FilteringRenderer.getFilterRenderState(be, blockState, itemModelManager, distance);
        if (state.filter != null) {
            state.blockPos = pos;
            state.blockState = blockState;
            state.blockEntityType = be.getType();
            state.lightCoords = SmartBlockEntityRenderer.getLightCoords(level, state.blockPos);
        }
        ItemStack item = be.getItem();
        if (item.isEmpty()) {
            return;
        }
        float itemPosition = be.itemPosition.getValue(tickProgress);
        if (itemPosition > 0) {
            return;
        }
        if (state.filter == null) {
            state.blockPos = pos;
            state.blockEntityType = be.getType();
        }
        state.item = ChuteItemRenderState.create(itemModelManager, item, itemPosition, level);
    }

    @Override
    public void submit(
        SmartChuteRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.filter != null) {
            state.filter.submit(state.blockState, queue, matrices, state.lightCoords);
        }
        if (state.item != null) {
            state.item.submit(matrices, queue, state.lightCoords);
        }
    }

    public static class SmartChuteRenderState extends BlockEntityRenderState {
        public @Nullable FilterRenderState filter;
        public @Nullable ChuteItemRenderState item;
    }
}
