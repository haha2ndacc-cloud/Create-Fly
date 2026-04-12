package com.zurrtum.create.client.content.kinetics.flywheel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.flywheel.FlywheelRenderer.FlywheelRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.flywheel.FlywheelBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;

public class FlywheelRenderer implements BlockEntityRenderer<FlywheelBlockEntity, FlywheelRenderState> {
    public FlywheelRenderer(Context context) {
    }

    @Override
    public FlywheelRenderState createRenderState() {
        return new FlywheelRenderState();
    }

    @Override
    public void extractRenderState(
        FlywheelBlockEntity be,
        FlywheelRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        Axis axis = getRotationAxisOf(state.blockState);
        Direction direction = axis.getPositive();
        int color = getTintColor(be);
        state.shaft = CachedBuffers.block(KINETIC_BLOCK, shaft(axis)).cardinalLighting(cardinalLighting)
            .light(state.lightCoords).color(color).extractRenderState();
        state.angle = getRotateAngleWithoutBeOffset(axis, direction, be, state, level);
        state.wheel = CachedBuffers.partialFacingVertical(AllPartialModels.FLYWHEEL, state.blockState, direction)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).color(color).extractRenderState();
        float speed = be.visualSpeed.getValue(tickProgress) * 0.3f;
        float angle = be.angle + speed * tickProgress;
        state.wheelAngle = getRotateAngle(angle, direction);
    }

    @Override
    public void submit(
        FlywheelRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.angle != null) {
            matrices.pushPose();
            matrices.rotateAround(state.angle, 0.5f, 0.5f, 0.5f);
            state.shaft.submit(matrices, queue);
            matrices.popPose();
        } else {
            state.shaft.submit(matrices, queue);
        }
        if (state.wheelAngle != null) {
            matrices.rotateAround(state.wheelAngle, 0.5f, 0.5f, 0.5f);
        }
        state.wheel.submit(matrices, queue);
    }

    public static class FlywheelRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState shaft;
        public @Nullable Quaternionf angle;
        public @UnknownNullability SuperByteBufferRenderState wheel;
        public @Nullable Quaternionf wheelAngle;
    }
}
