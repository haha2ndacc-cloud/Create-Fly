package com.zurrtum.create.client.content.kinetics.crank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.crank.ValveHandleRenderer.ValveHandleRenderState;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.crank.ValveHandleBlock;
import com.zurrtum.create.content.kinetics.crank.ValveHandleBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual.rotationOffset;

public class ValveHandleRenderer implements BlockEntityRenderer<ValveHandleBlockEntity, ValveHandleRenderState> {
    public ValveHandleRenderer(Context context) {
    }

    @Override
    public ValveHandleRenderState createRenderState() {
        return new ValveHandleRenderState();
    }

    @Override
    public void extractRenderState(
        ValveHandleBlockEntity be,
        ValveHandleRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        Direction facing = state.blockState.getValue(BlockStateProperties.FACING);
        Axis axis = facing.getAxis();
        Direction direction = axis.getPositive();
        if (be.inUse == 0 && be.source != null) {
            float speed = be.getSpeed();
            if (speed != 0) {
                state.angle = getRotateAngle(
                    getProgress(speed, level),
                    rotationOffset(state.blockState, axis, state.blockPos),
                    direction
                );
            } else {
                state.angle = getRotateAngle(getValveHandleIndependentAngle(be, facing, tickProgress), direction);
            }
        } else {
            state.angle = getRotateAngle(getValveHandleIndependentAngle(be, facing, tickProgress), direction);
        }
        PartialModel model;
        if (state.blockState.getBlock() instanceof ValveHandleBlock vhb && vhb.color != null) {
            model = AllPartialModels.DYED_VALVE_HANDLES.get(vhb.color);
        } else {
            model = AllPartialModels.VALVE_HANDLE;
        }
        state.model = CachedBuffers.partialFacingVertical(model, state.blockState, facing).cardinalLighting(level)
            .light(state.lightCoords).color(getTintColor(be)).extractRenderState();
    }

    @Override
    public void submit(
        ValveHandleRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState camera
    ) {
        if (state.angle != null) {
            matrices.rotateAround(state.angle, 0.5f, 0.5f, 0.5f);
        }
        state.model.submit(matrices, queue);
    }

    public static float getValveHandleIndependentAngle(
        ValveHandleBlockEntity be,
        Direction facing,
        float partialTicks
    ) {
        return (be.inUse > 0 && be.totalUseTicks > 0 ?
            Mth.lerp(
                Math.min(be.totalUseTicks, be.totalUseTicks - be.inUse + partialTicks) / be.totalUseTicks,
                be.startAngle,
                be.targetAngle
            ) : be.targetAngle) * (be.backwards ? -1 : 1) * facing.getAxisDirection().getStep();
    }

    public static class ValveHandleRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState model;
        public @Nullable Quaternionf angle;
    }
}
