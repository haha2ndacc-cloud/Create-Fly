package com.zurrtum.create.client.content.kinetics.fan;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.fan.EncasedFanRenderer.EncasedFanRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.fan.EncasedFanBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getProgress;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getRotateAngle;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual.rotationOffset;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class EncasedFanRenderer implements BlockEntityRenderer<EncasedFanBlockEntity, EncasedFanRenderState> {
    public EncasedFanRenderer(Context context) {
    }

    @Override
    public EncasedFanRenderState createRenderState() {
        return new EncasedFanRenderState();
    }

    @Override
    public void extractRenderState(
        EncasedFanBlockEntity be,
        EncasedFanRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        Direction facing = state.blockState.getValue(FACING);
        Direction opposite = facing.getOpposite();
        int lightBehind, lightInFront;
        if (level != null) {
            lightBehind = LightCoordsUtil.getLightCoords(level, state.blockPos.relative(opposite));
            lightInFront = LightCoordsUtil.getLightCoords(level, state.blockPos.relative(facing));
        } else {
            lightBehind = lightInFront = LightCoordsUtil.FULL_BRIGHT;
        }
        int color = KineticBlockEntityRenderer.getTintColor(be);
        state.shaftHalf = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state.blockState, opposite)
            .cardinalLighting(cardinalLighting).light(lightBehind).color(color).extractRenderState();
        state.fanInner = CachedBuffers.partialFacing(AllPartialModels.ENCASED_FAN_INNER, state.blockState, opposite)
            .cardinalLighting(cardinalLighting).light(lightInFront).color(color).extractRenderState();
        Axis axis = facing.getAxis();
        Direction direction = axis.getPositive();
        float speed = be.getSpeed();
        float time = AnimationTickHolder.getRenderTime(level);
        float offset = rotationOffset(state.blockState, axis, state.blockPos);
        state.angle = getRotateAngle(getProgress(speed, time), offset, direction);
        speed *= 5;
        if (speed > 0) {
            speed = Mth.clamp(speed, 80, 1280);
        } else if (speed < 0) {
            speed = Mth.clamp(speed, -1280, -80);
        }
        state.fanAngle = getRotateAngle(getProgress(speed, time) % 360, offset, direction);
    }

    @Override
    public void submit(
        EncasedFanRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.angle != null) {
            matrices.pushPose();
            matrices.rotateAround(state.angle, 0.5f, 0.5f, 0.5f);
            state.shaftHalf.submit(matrices, queue);
            matrices.popPose();
        } else {
            state.shaftHalf.submit(matrices, queue);
        }
        if (state.fanAngle != null) {
            matrices.rotateAround(state.fanAngle, 0.5f, 0.5f, 0.5f);
        }
        state.fanInner.submit(matrices, queue);
    }

    public static class EncasedFanRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState shaftHalf;
        public @UnknownNullability SuperByteBufferRenderState fanInner;
        public @Nullable Quaternionf angle;
        public @Nullable Quaternionf fanAngle;
    }
}
