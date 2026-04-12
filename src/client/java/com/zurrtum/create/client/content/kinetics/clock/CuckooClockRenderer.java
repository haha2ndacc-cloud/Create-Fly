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
        float doorAngle = 0;
        if (be.animationType != null) {
            float value = be.animationProgress.getValue(tickProgress);
            int step = be.animationType == Animation.SURPRISE ? 3 : 15;
            for (int phase = 30; phase <= 60; phase += step) {
                float local = value - phase;
                if (local < -step / 3) {
                    continue;
                }
                if (local < 0) {
                    doorAngle = Mth.lerp((value - (phase - 5)) / 5, 0, 135);
                } else if (local < step / 3) {
                    doorAngle = 135;
                } else if (local < 2 * step / 3) {
                    doorAngle = Mth.lerp((value - (phase + 5)) / 5, 135, 0);
                }
            }
        }
        if (doorAngle != 0) {
            float radians = Mth.DEG_TO_RAD * doorAngle;
            state.leftDoorAngle = new Quaternionf().setAngleAxis(-radians, 0, 1, 0);
            state.rightDoorAngle = new Quaternionf().setAngleAxis(radians, 0, 1, 0);
            if (be.animationType == Animation.NONE) {
                return;
            }
            state.offset = -(doorAngle / 135) * 0.5f + 0.625f;
            if (state.offset > 0.4f) {
                return;
            }
            PartialModel partialModel = be.animationType == Animation.PIG ? AllPartialModels.CUCKOO_PIG : AllPartialModels.CUCKOO_CREEPER;
            state.figure = CachedBuffers.partial(partialModel, state.blockState).cardinalLighting(cardinalLighting)
                .light(state.lightCoords).extractRenderState();
        }
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
