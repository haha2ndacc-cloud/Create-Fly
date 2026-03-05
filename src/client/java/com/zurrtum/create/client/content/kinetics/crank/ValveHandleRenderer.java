package com.zurrtum.create.client.content.kinetics.crank;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.KineticRenderState;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.kinetics.crank.ValveHandleBlock;
import com.zurrtum.create.content.kinetics.crank.ValveHandleBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ValveHandleRenderer extends KineticBlockEntityRenderer<ValveHandleBlockEntity, KineticRenderState> {
    public ValveHandleRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void extractRenderState(
        ValveHandleBlockEntity be,
        KineticRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        updateBaseRenderState(be, state, be.getLevel(), crumblingOverlay);
        state.model = getRenderedHandle(state.blockState);
        state.angle = AngleHelper.rad(getValveHandleIndependentAngle(be, tickProgress));
    }

    @Override
    protected RenderType getRenderType(ValveHandleBlockEntity be, BlockState state) {
        return RenderTypes.solidMovingBlock();
    }

    public static float getValveHandleIndependentAngle(ValveHandleBlockEntity be, float partialTicks) {
        if (be.inUse == 0 && be.source != null && be.getSpeed() != 0) {
            return AngleHelper.deg(KineticBlockEntityRenderer.getAngleForBe(
                be,
                be.getBlockPos(),
                KineticBlockEntityRenderer.getRotationAxisOf(be)
            ));
        }

        int step = be.getBlockState().getOptionalValue(ValveHandleBlock.FACING).orElse(Direction.SOUTH)
            .getAxisDirection().getStep();

        return (be.inUse > 0 && be.totalUseTicks > 0 ? Mth.lerpInt(
            Math.min(be.totalUseTicks, be.totalUseTicks - be.inUse + partialTicks) / (float) be.totalUseTicks,
            be.startAngle,
            be.targetAngle
        ) : be.targetAngle) * (be.backwards ? -1 : 1) * step;
    }

    public SuperByteBuffer getRenderedHandle(BlockState blockState) {
        PartialModel model;
        if (blockState.getBlock() instanceof ValveHandleBlock vhb && vhb.color != null) {
            model = AllPartialModels.DYED_VALVE_HANDLES.get(vhb.color);
        } else {
            model = AllPartialModels.VALVE_HANDLE;
        }
        return CachedBuffers.partialFacingVertical(model, blockState, blockState.getValue(BlockStateProperties.FACING));
    }
}
