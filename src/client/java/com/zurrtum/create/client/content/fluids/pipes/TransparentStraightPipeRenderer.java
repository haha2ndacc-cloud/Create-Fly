package com.zurrtum.create.client.content.fluids.pipes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.content.fluids.pipes.TransparentStraightPipeRenderer.TransparentStraightPipeRenderState;
import com.zurrtum.create.client.foundation.fluid.FluidRenderer;
import com.zurrtum.create.client.foundation.fluid.FluidRenderer.FluidStreamRenderState;
import com.zurrtum.create.content.fluids.FluidTransportBehaviour;
import com.zurrtum.create.content.fluids.PipeConnection.Flow;
import com.zurrtum.create.content.fluids.pipes.StraightPipeBlockEntity;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TransparentStraightPipeRenderer implements BlockEntityRenderer<StraightPipeBlockEntity, TransparentStraightPipeRenderState> {
    protected final FluidStateModelSet fluidStateModelSet;

    public TransparentStraightPipeRenderer(Context context) {
        fluidStateModelSet = context.blockModelResolver().modelManager.getFluidStateModelSet();
    }

    @Override
    public TransparentStraightPipeRenderState createRenderState() {
        return new TransparentStraightPipeRenderState();
    }

    @Override
    public void extractRenderState(
        StraightPipeBlockEntity be,
        TransparentStraightPipeRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        FluidTransportBehaviour pipe = be.getBehaviour(FluidTransportBehaviour.TYPE);
        if (pipe == null) {
            return;
        }
        Direction[] directions = Iterate.directions;
        List<FluidStreamRenderState> fluids = new ArrayList<>(directions.length);
        BlockAndTintGetter level = (BlockAndTintGetter) be.getLevel();
        BlockPos blockPos = be.getBlockPos();
        int lightCoords = level != null ? LightCoordsUtil.getLightCoords(level, blockPos) : LightCoordsUtil.FULL_BRIGHT;
        float radius = 0.1875f;
        for (Direction side : directions) {
            Flow flow = pipe.getFlow(side);
            if (flow == null) {
                continue;
            }
            FluidStack fluidStack = flow.fluid;
            if (fluidStack.isEmpty()) {
                continue;
            }
            LerpedFloat progress = flow.progress;
            if (progress == null) {
                continue;
            }
            float value = progress.getValue(tickProgress);
            boolean inbound = flow.inbound;
            if (value == 1) {
                if (inbound) {
                    Flow opposite = pipe.getFlow(side.getOpposite());
                    if (opposite == null) {
                        value -= 1.0e-6f;
                    }
                } else {
                    FluidTransportBehaviour adjacent = BlockEntityBehaviour.get(
                        level,
                        blockPos.relative(side),
                        FluidTransportBehaviour.TYPE
                    );
                    if (adjacent == null) {
                        value -= 1.0e-6f;
                    } else {
                        Flow other = adjacent.getFlow(side.getOpposite());
                        if (other == null || !other.inbound && !other.complete) {
                            value -= 1.0e-6f;
                        }
                    }
                }
            }
            fluids.add(FluidRenderer.extractFluidStreamRenderState(
                level,
                blockPos,
                fluidStateModelSet,
                fluidStack.getFluid(),
                fluidStack.getComponentChanges(),
                side,
                value,
                inbound,
                radius,
                lightCoords
            ));
        }
        if (fluids.isEmpty()) {
            return;
        }
        state.blockPos = blockPos;
        state.blockState = be.getBlockState();
        state.blockEntityType = be.getType();
        state.lightCoords = lightCoords;
        state.breakProgress = crumblingOverlay;
        state.fluids = fluids;
    }

    @Override
    public void submit(
        TransparentStraightPipeRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.fluids != null) {
            for (FluidStreamRenderState fluid : state.fluids) {
                fluid.submit(queue, matrices);
            }
        }
    }

    public static class TransparentStraightPipeRenderState extends BlockEntityRenderState {
        public @Nullable List<FluidStreamRenderState> fluids;
    }
}
