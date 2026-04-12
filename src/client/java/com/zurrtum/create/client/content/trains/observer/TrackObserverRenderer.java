package com.zurrtum.create.client.content.trains.observer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllTrackRenders;
import com.zurrtum.create.client.content.trains.observer.TrackObserverRenderer.TrackObserverRenderState;
import com.zurrtum.create.client.content.trains.track.TrackBlockRenderState;
import com.zurrtum.create.client.content.trains.track.TrackBlockRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer.FilterRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.trains.observer.TrackObserver;
import com.zurrtum.create.content.trains.observer.TrackObserverBlockEntity;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class TrackObserverRenderer implements BlockEntityRenderer<TrackObserverBlockEntity, TrackObserverRenderState> {
    protected final ItemModelResolver itemModelManager;

    public TrackObserverRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public TrackObserverRenderState createRenderState() {
        return new TrackObserverRenderState();
    }

    @Override
    public void extractRenderState(
        TrackObserverBlockEntity be,
        TrackObserverRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level world = be.getLevel();
        BlockPos blockPos = be.getBlockPos();
        BlockState blockState = be.getBlockState();
        state.filter = FilteringRenderer.getFilterRenderState(
            be,
            blockState,
            itemModelManager,
            be.isVirtual() ? -1 : cameraPos.distanceToSqr(VecHelper.getCenterOf(blockPos))
        );
        if (!VisualizationManager.supportsVisualization(world)) {
            TrackTargetingBehaviour<TrackObserver> target = be.edgePoint;
            BlockPos targetPosition = target.getGlobalPosition();
            BlockState trackState = world.getBlockState(targetPosition);
            Block block = trackState.getBlock();
            if (block instanceof ITrackBlock track) {
                TrackBlockRenderer renderer = AllTrackRenders.get(track);
                if (renderer != null) {
                    state.block = renderer.getRenderState(
                        world,
                        new Vec3(
                            targetPosition.getX() - blockPos.getX(),
                            targetPosition.getY() - blockPos.getY(),
                            targetPosition.getZ() - blockPos.getZ()
                        ),
                        trackState,
                        targetPosition,
                        target.getTargetDirection(),
                        target.getTargetBezier(),
                        RenderedTrackOverlayType.OBSERVER,
                        1
                    );
                }
            }
        }
        if (state.filter != null || state.block != null) {
            state.blockPos = blockPos;
            state.blockState = blockState;
            state.lightCoords = SmartBlockEntityRenderer.getLightCoords(be.getLevel(), blockPos);
            state.blockEntityType = be.getType();
        }
    }

    @Override
    public void submit(
        TrackObserverRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.filter != null) {
            state.filter.submit(state.blockState, queue, matrices, state.lightCoords);
        }
        if (state.block != null) {
            state.block.submit(matrices, queue);
        }
    }

    public static class TrackObserverRenderState extends BlockEntityRenderState {
        public @Nullable FilterRenderState filter;
        public @Nullable TrackBlockRenderState block;
    }
}
