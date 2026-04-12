package com.zurrtum.create.client.content.contraptions.actors.contraptionControls;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.contraptions.actors.contraptionControls.ContraptionControlsRenderer.ContraptionControlsRenderState;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer.FilterRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsBlock;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public class ContraptionControlsRenderer implements BlockEntityRenderer<ContraptionControlsBlockEntity, ContraptionControlsRenderState> {
    protected final ItemModelResolver itemModelManager;

    public ContraptionControlsRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public ContraptionControlsRenderState createRenderState() {
        return new ContraptionControlsRenderState();
    }

    @Override
    public void extractRenderState(
        ContraptionControlsBlockEntity be,
        ContraptionControlsRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        state.filter = FilteringRenderer.getFilterRenderState(
            be,
            state.blockState,
            itemModelManager,
            be.isVirtual() ? -1 : cameraPos.distanceToSqr(VecHelper.getCenterOf(state.blockPos))
        );
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        Direction facing = state.blockState.getValue(ContraptionControlsBlock.FACING).getOpposite();
        Vec3 buttonMovementAxis = VecHelper.rotate(new Vec3(0, 1, -0.325), AngleHelper.horizontalAngle(facing), Axis.Y);
        state.buttonMovement = buttonMovementAxis.scale(-0.07f + -1 / 24.0f * be.button.getValue(tickProgress));
        state.buttonOffset = buttonMovementAxis.scale(0.07f);
        state.button = CachedBuffers.partialFacing(
            AllPartialModels.CONTRAPTION_CONTROLS_BUTTON,
            state.blockState,
            facing
        ).cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        int i = (int) be.indicator.getValue(tickProgress) / 45 % 8 + 8;
        state.indicator = CachedBuffers.partialFacing(
            AllPartialModels.CONTRAPTION_CONTROLS_INDICATOR.get(i % 8),
            state.blockState,
            facing
        ).cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
    }

    @Override
    public void submit(
        ContraptionControlsRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        matrices.pushPose();
        matrices.translate(state.buttonMovement);
        if (state.filter != null) {
            state.filter.submit(state.blockState, queue, matrices, state.lightCoords);
        }
        matrices.translate(state.buttonOffset);
        state.button.submit(matrices, queue);
        matrices.popPose();
        state.indicator.submit(matrices, queue);
    }

    public static class ContraptionControlsRenderState extends BlockEntityRenderState {
        public @Nullable FilterRenderState filter;
        public @UnknownNullability Vec3 buttonMovement;
        public @UnknownNullability Vec3 buttonOffset;
        public @UnknownNullability SuperByteBufferRenderState button;
        public @UnknownNullability SuperByteBufferRenderState indicator;
    }
}
