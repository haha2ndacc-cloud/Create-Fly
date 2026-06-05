package com.zurrtum.create.client.content.trains.signal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllTrackRenders;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.trains.signal.SignalRenderer.SignalRenderState;
import com.zurrtum.create.client.content.trains.track.TrackBlockRenderState;
import com.zurrtum.create.client.content.trains.track.TrackBlockRenderer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity.OverlayState;
import com.zurrtum.create.content.trains.signal.SignalBoundary;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public class SignalRenderer implements BlockEntityRenderer<SignalBlockEntity, SignalRenderState> {
    public SignalRenderer(Context context) {
    }

    @Override
    public SignalRenderState createRenderState() {
        return new SignalRenderState();
    }

    @Override
    public void extractRenderState(
        SignalBlockEntity be,
        SignalRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        state.blockPos = be.getBlockPos();
        state.blockState = be.getBlockState();
        state.blockEntityType = be.getType();
        state.breakProgress = crumblingOverlay;
        Level level = be.getLevel();
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        float renderTime = AnimationTickHolder.getRenderTime(level);
        if (be.getState().isRedLight(renderTime)) {
            state.lightCoords = LightCoordsUtil.MAX_SMOOTH_LIGHT_LEVEL;
            state.model = CachedBuffers.partial(AllPartialModels.SIGNAL_ON, state.blockState)
                .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        } else {
            state.lightCoords = SmartBlockEntityRenderer.getLightCoords(level, state.blockPos);
            state.model = CachedBuffers.partial(AllPartialModels.SIGNAL_OFF, state.blockState)
                .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        }
        TrackTargetingBehaviour<SignalBoundary> target = be.edgePoint;
        BlockPos targetPosition = target.getGlobalPosition();
        BlockState trackState = level.getBlockState(targetPosition);
        Block block = trackState.getBlock();
        if (!(block instanceof ITrackBlock trackBlock)) {
            return;
        }
        OverlayState overlayState = be.getOverlay();
        if (overlayState == OverlayState.SKIP) {
            return;
        }
        TrackBlockRenderer renderer = AllTrackRenders.get(trackBlock);
        if (renderer != null) {
            RenderedTrackOverlayType type = overlayState == OverlayState.DUAL ? RenderedTrackOverlayType.DUAL_SIGNAL :
                RenderedTrackOverlayType.SIGNAL;
            state.block = renderer.getRenderState(
                level, new Vec3(
                    targetPosition.getX() - state.blockPos.getX(),
                    targetPosition.getY() - state.blockPos.getY(),
                    targetPosition.getZ() - state.blockPos.getZ()
                ), trackState, targetPosition, target.getTargetDirection(), target.getTargetBezier(), type, 1
            );
        }
    }

    @Override
    public void submit(
        SignalRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        state.model.submit(matrices, queue);
        if (state.block != null) {
            state.block.submit(matrices, queue);
        }
    }

    public static class SignalRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState model;
        public @Nullable TrackBlockRenderState block;
    }
}
