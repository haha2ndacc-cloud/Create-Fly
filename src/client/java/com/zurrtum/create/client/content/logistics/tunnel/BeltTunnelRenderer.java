package com.zurrtum.create.client.content.logistics.tunnel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.logistics.FlapStuffs;
import com.zurrtum.create.client.content.logistics.FlapStuffs.FlapsRenderState;
import com.zurrtum.create.client.content.logistics.tunnel.BeltTunnelRenderer.BeltTunnelRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer.FilterRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.logistics.tunnel.BeltTunnelBlockEntity;
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

import java.util.ArrayList;
import java.util.List;

public class BeltTunnelRenderer implements BlockEntityRenderer<BeltTunnelBlockEntity, BeltTunnelRenderState> {
    protected final ItemModelResolver itemModelManager;

    public BeltTunnelRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public BeltTunnelRenderState createRenderState() {
        return new BeltTunnelRenderState();
    }

    @Override
    public void extractRenderState(
        BeltTunnelBlockEntity be,
        BeltTunnelRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = be.getLevel();
        if (VisualizationManager.supportsVisualization(level) || be.flaps.isEmpty()) {
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
        SuperByteBufferRenderState flapBuffer = CachedBuffers.partial(
            AllPartialModels.BELT_TUNNEL_FLAP,
            state.blockState
        ).cardinalLighting(level).light(state.lightCoords).extractRenderState();
        List<FlapsRenderState> flaps = state.flaps = new ArrayList<>(be.flaps.size());
        be.flaps.forEach((direction, lerpedFloat) -> flaps.add(FlapStuffs.getFlapsRenderState(
            flapBuffer,
            FlapStuffs.TUNNEL_PIVOT,
            direction,
            lerpedFloat.getValue(tickProgress),
            0
        )));
        state.filter = FilteringRenderer.getFilterRenderState(
            be,
            state.blockState,
            itemModelManager,
            be.isVirtual() ? -1 : cameraPos.distanceToSqr(VecHelper.getCenterOf(state.blockPos))
        );
    }

    @Override
    public void submit(
        BeltTunnelRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.filter != null) {
            state.filter.submit(state.blockState, queue, matrices, state.lightCoords);
        }
        if (state.flaps != null) {
            for (FlapsRenderState flap : state.flaps) {
                flap.submit(matrices, queue);
            }
        }
    }

    public static class BeltTunnelRenderState extends BlockEntityRenderState {
        public @Nullable FilterRenderState filter;
        public @Nullable List<FlapsRenderState> flaps;
    }
}
