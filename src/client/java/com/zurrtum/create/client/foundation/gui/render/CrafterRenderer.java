package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.platform.Lighting.Entry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.render.BlockBakedQuadOutput;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelRenderHelper;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CrafterRenderer extends PictureInPictureRenderer<CrafterRenderState> {
    private final BlockBakedQuadOutput output;

    public CrafterRenderer(BufferSource vertexConsumers) {
        super(vertexConsumers);
        output = new BlockBakedQuadOutput(vertexConsumers);
    }

    @Override
    protected void renderToTexture(CrafterRenderState state, PoseStack matrices) {
        Minecraft mc = Minecraft.getInstance();
        mc.gameRenderer.lighting().setupFor(Entry.ENTITY_IN_UI);
        matrices.scale(1, 1, -1);
        matrices.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrices.mulPose(Axis.YP.rotationDegrees(-22.5f));
        matrices.translate(-0.5f, -0.16f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockState blockState;
        BlockStateModel model;
        output.setPoseStack(matrices);
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();

        blockState = Blocks.AIR.defaultBlockState();
        world.blockState(blockState);
        matrices.pushPose();
        model = AllPartialModels.SHAFTLESS_COGWHEEL.get();
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.mulPose(Axis.ZP.rotationDegrees(getCurrentAngle()));
        matrices.mulPose(Axis.XP.rotationDegrees(90));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        output.updateBuffer(model);
        ModelConsumer blockRenderer = ModelRenderHelper.getHelper(output);
        blockRenderer.tesselateBlock(0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
        matrices.popPose();

        blockState = AllBlocks.MECHANICAL_CRAFTER.defaultBlockState();
        world.blockState(blockState);
        matrices.pushPose();
        model = mc.getModelManager().getBlockStateModelSet().get(blockState);
        matrices.rotateAround(Axis.YP.rotationDegrees(180), 0.5f, 0.5f, 0.5f);
        output.updateBuffer(model);
        blockRenderer.tesselateBlock(0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
        matrices.popPose();
        output.clear();
    }

    public static float getCurrentAngle() {
        return AnimationTickHolder.getRenderTime() * 4.0f % 360;
    }

    @Override
    protected String getTextureLabel() {
        return "Crafter";
    }

    @Override
    public Class<CrafterRenderState> getRenderStateClass() {
        return CrafterRenderState.class;
    }
}
