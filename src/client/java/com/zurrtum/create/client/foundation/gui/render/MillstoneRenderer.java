package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.render.BlockBakedQuadOutput;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MillstoneRenderer extends PictureInPictureRenderer<MillstoneRenderState> {
    private final BlockBakedQuadOutput output;
    private final ModelBlockRenderer blockRenderer;

    public MillstoneRenderer(BufferSource vertexConsumers) {
        super(vertexConsumers);
        output = new BlockBakedQuadOutput(vertexConsumers);
        Minecraft minecraft = Minecraft.getInstance();
        boolean ambientOcclusion = minecraft.options.ambientOcclusion().get();
        blockRenderer = new ModelBlockRenderer(ambientOcclusion, false, minecraft.getBlockColors());
    }

    @Override
    protected void renderToTexture(MillstoneRenderState state, PoseStack matrices) {
        Minecraft mc = Minecraft.getInstance();
        mc.gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        matrices.translate(-0.5f, -0.21f, -0.5f);
        matrices.scale(1, -1, -1);
        BlockState blockState;
        BlockStateModel model;
        output.setPoseStack(matrices);
        BlockRenderDispatcher blockRenderManager = mc.getBlockRenderer();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();

        matrices.pushPose();
        blockState = Blocks.AIR.defaultBlockState();
        world.blockState(blockState);
        model = AllPartialModels.MILLSTONE_COG.get();
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.mulPose(Axis.XP.rotationDegrees(22.5f));
        matrices.mulPose(Axis.YP.rotationDegrees(getCurrentAngle()));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        output.updateBuffer(model);
        blockRenderer.tesselateBlock(output, 0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
        matrices.popPose();

        blockState = AllBlocks.MILLSTONE.defaultBlockState();
        world.blockState(blockState);
        model = blockRenderManager.getBlockModel(blockState);
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.mulPose(Axis.XP.rotationDegrees(22.5f));
        matrices.mulPose(Axis.YP.rotationDegrees(22.5f));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        output.updateBuffer(model);
        blockRenderer.tesselateBlock(output, 0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
        output.clear();
    }

    private static float getCurrentAngle() {
        return (AnimationTickHolder.getRenderTime() * 4f) % 360 * 2;
    }

    @Override
    protected String getTextureLabel() {
        return "Millstone";
    }

    @Override
    public Class<MillstoneRenderState> getRenderStateClass() {
        return MillstoneRenderState.class;
    }
}
