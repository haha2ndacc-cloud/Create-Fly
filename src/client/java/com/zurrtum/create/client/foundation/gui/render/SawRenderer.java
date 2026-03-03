package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.render.BlockBakedQuadOutput;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import com.zurrtum.create.content.kinetics.saw.SawBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class SawRenderer extends PictureInPictureRenderer<SawRenderState> {
    private final BlockBakedQuadOutput output;
    private final ModelBlockRenderer blockRenderer;

    public SawRenderer(BufferSource vertexConsumers) {
        super(vertexConsumers);
        output = new BlockBakedQuadOutput(vertexConsumers);
        Minecraft minecraft = Minecraft.getInstance();
        boolean ambientOcclusion = minecraft.options.ambientOcclusion().get();
        blockRenderer = new ModelBlockRenderer(ambientOcclusion, false, minecraft.getBlockColors());
    }

    @Override
    protected void renderToTexture(SawRenderState state, PoseStack matrices) {
        Minecraft mc = Minecraft.getInstance();
        mc.gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        matrices.scale(1, 1, -1);
        matrices.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrices.mulPose(Axis.YP.rotationDegrees(112.5f));
        matrices.translate(-0.5f, -0.2f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockState blockState;
        BlockStateModel model;
        output.setPoseStack(matrices);
        BlockRenderDispatcher blockRenderManager = mc.getBlockRenderer();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();

        matrices.pushPose();
        blockState = AllBlocks.SHAFT.defaultBlockState()
            .setValue(BlockStateProperties.AXIS, net.minecraft.core.Direction.Axis.X);
        world.blockState(blockState);
        model = blockRenderManager.getBlockModel(blockState);
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.mulPose(Axis.XP.rotationDegrees(getCurrentAngle()));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        output.updateBuffer(model);
        blockRenderer.tesselateBlock(output, 0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
        matrices.popPose();

        blockState = AllBlocks.MECHANICAL_SAW.defaultBlockState().setValue(SawBlock.FACING, Direction.UP);
        world.blockState(blockState);
        model = blockRenderManager.getBlockModel(blockState);
        output.updateBuffer(model);
        blockRenderer.tesselateBlock(output, 0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);

        blockState = Blocks.AIR.defaultBlockState();
        world.blockState(blockState);
        model = AllPartialModels.SAW_BLADE_VERTICAL_ACTIVE.get();
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.mulPose(Axis.ZP.rotationDegrees(-90));
        matrices.mulPose(Axis.YP.rotationDegrees(-90));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        output.updateBuffer(model);
        blockRenderer.tesselateBlock(output, 0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
        output.clear();
    }

    public static float getCurrentAngle() {
        return -(AnimationTickHolder.getRenderTime() * 4f) % 360;
    }

    @Override
    protected String getTextureLabel() {
        return "Saw";
    }

    @Override
    public Class<SawRenderState> getRenderStateClass() {
        return SawRenderState.class;
    }

}
