package com.zurrtum.create.client.content.kinetics.clock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.clock.CuckooClockRenderer.CuckooClockRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.animation.AnimationBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.animation.CuckooClockAnimationBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.clock.CuckooClockBlock;
import com.zurrtum.create.content.kinetics.clock.CuckooClockBlockEntity;
import com.zurrtum.create.content.kinetics.clock.CuckooClockBlockEntity.Animation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;

public class CuckooClockRenderer implements BlockEntityRenderer<CuckooClockBlockEntity, CuckooClockRenderState> {
    public CuckooClockRenderer(Context context) {
    }

    @Override
    public CuckooClockRenderState createRenderState() {
        return new CuckooClockRenderState();
    }

    @Override
    public void extractRenderState(
        CuckooClockBlockEntity be,
        CuckooClockRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        Direction facing = state.blockState.getValue(CuckooClockBlock.HORIZONTAL_FACING);
        if (!VisualizationManager.supportsVisualization(level)) {
            state.shaft = CachedBuffers.partialFacing(
                AllPartialModels.SHAFT_HALF,
                state.blockState,
                facing.getOpposite()
            ).cardinalLighting(cardinalLighting).light(state.lightCoords).color(getTintColor(be)).extractRenderState();
            state.angle = getRotateAngleWithoutBeOffset(facing.getAxis(), be, state, level);
        }
        state.hourHand = CachedBuffers.partial(AllPartialModels.CUCKOO_HOUR_HAND, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        state.minuteHand = CachedBuffers.partial(AllPartialModels.CUCKOO_MINUTE_HAND, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        CuckooClockAnimationBehaviour behaviour = (CuckooClockAnimationBehaviour) be.getBehaviour(AnimationBehaviour.TYPE);
        if (behaviour != null) {
            state.hourAngle = getEastRotateAngle(behaviour.hourHand.getValue(tickProgress));
            state.minuteAngle = getEastRotateAngle(behaviour.minuteHand.getValue(tickProgress));
        }
        state.upAngle = getUpRotateAngle(AngleHelper.horizontalAngle(facing.getCounterClockWise()));
        state.leftDoor = CachedBuffers.partial(AllPartialModels.CUCKOO_LEFT_DOOR, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        state.rightDoor = CachedBuffers.partial(AllPartialModels.CUCKOO_RIGHT_DOOR, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        float doorAngle = getDoorAngle(be, tickProgress);
        if (doorAngle != 0) {
            float radians = Mth.DEG_TO_RAD * doorAngle;
            state.rightDoorAngle = new Quaternionf().setAngleAxis(radians, 0, 1, 0);
            state.leftDoorAngle = new Quaternionf(state.rightDoorAngle).conjugate();
            if (be.animationType == Animation.NONE) {
                return;
            }
            state.offset = -(doorAngle / 135) * 0.5f + 0.625f;
            if (state.offset > 0.4f) {
                return;
            }
            PartialModel partialModel =
                be.animationType == Animation.PIG ? AllPartialModels.CUCKOO_PIG : AllPartialModels.CUCKOO_CREEPER;
            state.figure = CachedBuffers.partial(partialModel, state.blockState).cardinalLighting(cardinalLighting)
                .light(state.lightCoords).extractRenderState();
        }
    }

    public static float getDoorAngle(CuckooClockBlockEntity be, float tickProgress) {
        float value = be.animationProgress.getValue(tickProgress);
        if (value < 25 || value >= 70) {
            return 0;
        }
        int step, minValue;
        if (be.animationType != Animation.SURPRISE) {
            minValue = 25;
            step = 5;
        } else if (value >= 29 && value < 62) {
            minValue = 29;
            step = 1;
        } else {
            return 0;
        }
        float local = value - (int) value - step + ((int) value - minValue) % (step * 3);
        if (local < 0) {
            return Mth.lerp((local + 5) / 5.0f, 0, 135);
        }
        if (local < step) {
            return 135;
        }
        return Mth.lerp((local - 5) / 5.0f, 135, 0);
    }

    @Override
    public void submit(
        CuckooClockRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.shaft != null) {
            if (state.angle != null) {
                matrices.pushPose();
                matrices.rotateAround(state.angle, 0.5f, 0.5f, 0.5f);
                state.shaft.submit(matrices, queue);
                matrices.popPose();
            } else {
                state.shaft.submit(matrices, queue);
            }
        }
        if (state.upAngle != null) {
            matrices.rotateAround(state.upAngle, 0.5f, 0.5f, 0.5f);
        }
        if (state.hourAngle != null) {
            matrices.pushPose();
            matrices.rotateAround(state.hourAngle, 0.125f, 0.375f, 0.5f);
            state.hourHand.submit(matrices, queue);
            matrices.popPose();
        } else {
            state.hourHand.submit(matrices, queue);
        }
        if (state.minuteAngle != null) {
            matrices.pushPose();
            matrices.rotateAround(state.minuteAngle, 0.125f, 0.375f, 0.5f);
            state.minuteHand.submit(matrices, queue);
            matrices.popPose();
        } else {
            state.minuteHand.submit(matrices, queue);
        }
        if (state.leftDoorAngle != null) {
            matrices.pushPose();
            matrices.rotateAround(state.leftDoorAngle, 0.125f, 0, 0.375f);
            state.leftDoor.submit(matrices, queue);
            matrices.popPose();
            matrices.pushPose();
            matrices.rotateAround(state.rightDoorAngle, 0.125f, 0, 0.625f);
            state.rightDoor.submit(matrices, queue);
            matrices.popPose();
        } else {
            state.leftDoor.submit(matrices, queue);
            state.rightDoor.submit(matrices, queue);
        }
        if (state.figure != null) {
            matrices.translate(state.offset, 0, 0);
            state.figure.submit(matrices, queue);
        }
    }

    public static class CuckooClockRenderState extends BlockEntityRenderState {
        public @Nullable SuperByteBufferRenderState shaft;
        public @Nullable Quaternionf angle;
        public @UnknownNullability SuperByteBufferRenderState hourHand;
        public @UnknownNullability SuperByteBufferRenderState minuteHand;
        public @Nullable Quaternionf upAngle;
        public @Nullable Quaternionf hourAngle;
        public @Nullable Quaternionf minuteAngle;
        public @UnknownNullability SuperByteBufferRenderState leftDoor;
        public @UnknownNullability SuperByteBufferRenderState rightDoor;
        public @Nullable Quaternionf leftDoorAngle;
        public @UnknownNullability Quaternionf rightDoorAngle;
        public @Nullable SuperByteBufferRenderState figure;
        public float offset;
    }
}
