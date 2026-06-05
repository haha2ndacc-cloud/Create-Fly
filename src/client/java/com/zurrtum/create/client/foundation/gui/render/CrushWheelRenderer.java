package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class CrushWheelRenderer extends PictureInPictureRenderer<CrushWheelRenderState> {
    private final BlockState blockState = AllBlocks.CRUSHING_WHEEL.defaultBlockState()
        .setValue(BlockStateProperties.AXIS, Axis.X);

    @Override
    protected void renderToTexture(CrushWheelRenderState state, PoseStack matrices, SubmitNodeCollector queue) {
        matrices.scale(1, 1, -1);
        matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-22.5f));
        matrices.translate(-1.5f, -0.6f, -0.5f);
        matrices.scale(1, -1, 1);
        SuperByteBuffer model = CachedBuffers.block(blockState);

        float angle = getCurrentAngle();
        matrices.pushPose();
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-angle));
        matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        model.submit(matrices, queue);
        matrices.popPose();

        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.translate(2, 0, 0);
        matrices.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angle));
        matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        model.submit(matrices, queue);
    }

    public static float getCurrentAngle() {
        return AnimationTickHolder.getRenderTime() * 4.0f % 360;
    }

    @Override
    public Class<CrushWheelRenderState> getRenderStateClass() {
        return CrushWheelRenderState.class;
    }

    @Override
    protected String getTextureLabel() {
        return "Crush Wheel";
    }
}
