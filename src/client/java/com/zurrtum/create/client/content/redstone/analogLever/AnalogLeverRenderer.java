package com.zurrtum.create.client.content.redstone.analogLever;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.redstone.analogLever.AnalogLeverRenderer.AnalogLeverRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlock;
import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;

public class AnalogLeverRenderer implements BlockEntityRenderer<AnalogLeverBlockEntity, AnalogLeverRenderState> {
    public AnalogLeverRenderer(Context context) {
    }

    @Override
    public AnalogLeverRenderState createRenderState() {
        return new AnalogLeverRenderState();
    }

    @Override
    public void extractRenderState(
        AnalogLeverBlockEntity be,
        AnalogLeverRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level world = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(world);
        float level = be.clientState.getValue(tickProgress) / 15;
        state.angle = getEastRadiansRotateAngle((float) (level / 2 * Math.PI));
        state.handle = CachedBuffers.partial(AllPartialModels.ANALOG_LEVER_HANDLE, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        AttachFace face = state.blockState.getValue(AnalogLeverBlock.FACE);
        if (face != AttachFace.FLOOR) {
            state.xRot = new Quaternionf().setAngleAxis(face == AttachFace.WALL ? RAD_90 : RAD_180, 1, 0, 0);
        }
        state.yRot = getUpRotateAngle(AngleHelper.horizontalAngle(state.blockState.getValue(AnalogLeverBlock.FACING)));
        int color = Color.mixColors(0xFF2C0300, 0xFFCD0000, level);
        state.indicator = CachedBuffers.partial(AllPartialModels.ANALOG_LEVER_INDICATOR, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).color(color).extractRenderState();
    }

    @Override
    public void submit(
        AnalogLeverRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.yRot != null) {
            matrices.rotateAround(state.yRot, 0.5f, 0.5f, 0.5f);
        }
        if (state.xRot != null) {
            matrices.rotateAround(state.xRot, 0.5f, 0.5f, 0.5f);
        }
        state.indicator.submit(matrices, queue);
        if (state.angle != null) {
            matrices.rotateAround(state.angle, 0.5f, 0.0625f, 0.5f);
        }
        state.handle.submit(matrices, queue);
    }

    public static class AnalogLeverRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState handle;
        public @UnknownNullability SuperByteBufferRenderState indicator;
        public @Nullable Quaternionf angle;
        public @Nullable Quaternionf xRot;
        public @Nullable Quaternionf yRot;
    }
}
