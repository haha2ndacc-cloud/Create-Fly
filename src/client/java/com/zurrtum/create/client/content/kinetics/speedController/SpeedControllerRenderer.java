package com.zurrtum.create.client.content.kinetics.speedController;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.speedController.SpeedControllerRenderer.SpeedControllerRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.speedController.SpeedControllerBlock;
import com.zurrtum.create.content.kinetics.speedController.SpeedControllerBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;
import static com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer.getLightCoords;

public class SpeedControllerRenderer implements BlockEntityRenderer<SpeedControllerBlockEntity, SpeedControllerRenderState> {
    public SpeedControllerRenderer(Context context) {
    }

    @Override
    public SpeedControllerRenderState createRenderState() {
        return new SpeedControllerRenderState();
    }

    @Override
    public void extractRenderState(
        SpeedControllerBlockEntity be,
        SpeedControllerRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = be.getLevel();
        if (VisualizationManager.supportsVisualization(level)) {
            if (be.hasBracket) {
                state.blockPos = be.getBlockPos();
                state.blockState = be.getBlockState();
                state.blockEntityType = be.getType();
                CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
                updateBracketState(state, level, cardinalLighting);
            }
            return;
        }
        SmartBlockEntityRenderer.extractBase(level, be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        Axis axis = getRotationAxisOf(state.blockState);
        state.angle = getRotateAngleWithoutBeOffset(axis, be, state, level);
        state.model = CachedBuffers.block(KINETIC_BLOCK, shaft(axis)).cardinalLighting(cardinalLighting)
            .light(state.lightCoords).color(getTintColor(be)).extractRenderState();
        if (be.hasBracket) {
            updateBracketState(state, level, cardinalLighting);
        }
    }

    public static void updateBracketState(
        SpeedControllerRenderState state,
        @Nullable Level level,
        @Nullable CardinalLighting cardinalLighting
    ) {
        boolean alongX = state.blockState.getValue(SpeedControllerBlock.HORIZONTAL_AXIS) == Axis.X;
        state.bracketAngle = getUpRadiansRotateAngle((float) (alongX ? Math.PI : Math.PI / 2));
        state.bracket = CachedBuffers.partial(AllPartialModels.SPEED_CONTROLLER_BRACKET, state.blockState)
            .cardinalLighting(cardinalLighting).light(getLightCoords(level, state.blockPos.above()))
            .extractRenderState();
    }

    @Override
    public void submit(
        SpeedControllerRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.model != null) {
            if (state.angle != null) {
                matrices.pushPose();
                matrices.rotateAround(state.angle, 0.5f, 0.5f, 0.5f);
                state.model.submit(matrices, queue);
                matrices.popPose();
            } else {
                state.model.submit(matrices, queue);
            }
        }
        if (state.bracket != null) {
            matrices.translate(0, 1, 0);
            if (state.bracketAngle != null) {
                matrices.rotateAround(state.bracketAngle, 0.5f, 0.5f, 0.5f);
            }
            state.bracket.submit(matrices, queue);
        }
    }

    public static class SpeedControllerRenderState extends BlockEntityRenderState {
        public @Nullable SuperByteBufferRenderState model;
        public @Nullable SuperByteBufferRenderState bracket;
        public @Nullable Quaternionf angle;
        public @Nullable Quaternionf bracketAngle;
    }
}
