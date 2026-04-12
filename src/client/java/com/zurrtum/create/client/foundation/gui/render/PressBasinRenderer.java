package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Lighting.Entry;
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
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class PressBasinRenderer extends PictureInPictureRenderer<PressBasinRenderState> {
    private final BlockBakedQuadOutput output;
    private final ModelBlockRenderer blockRenderer;

    public PressBasinRenderer(BufferSource vertexConsumers) {
        super(vertexConsumers);
        output = new BlockBakedQuadOutput(vertexConsumers);
        Minecraft minecraft = Minecraft.getInstance();
        boolean ambientOcclusion = minecraft.options.ambientOcclusion().get();
        blockRenderer = new ModelBlockRenderer(ambientOcclusion, false, minecraft.getBlockColors());
    }

    @Override
    protected void renderToTexture(PressBasinRenderState state, PoseStack matrices) {
        Minecraft mc = Minecraft.getInstance();
        mc.gameRenderer.getLighting().setupFor(Entry.ENTITY_IN_UI);
        matrices.scale(1, 1, -1);
        matrices.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrices.mulPose(Axis.YP.rotationDegrees(22.5f));
        matrices.translate(-0.5f, -1.8f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockState blockState;
        BlockStateModel model;
        output.setPoseStack(matrices);
        BlockStateModelSet blockStateModelSet = mc.getModelManager().getBlockStateModelSet();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        float time = AnimationTickHolder.getRenderTime();

        blockState = AllBlocks.MECHANICAL_PRESS.defaultBlockState();
        world.blockState(blockState);
        model = blockStateModelSet.get(blockState);
        output.updateBuffer(model);
        blockRenderer.tesselateBlock(output, 0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);

        matrices.pushPose();
        blockState = AllBlocks.SHAFT.defaultBlockState()
            .setValue(BlockStateProperties.AXIS, Direction.Axis.Z);
        world.blockState(blockState);
        model = blockStateModelSet.get(blockState);
        matrices.rotateAround(Axis.ZP.rotationDegrees(getShaftAngle(time)), 0.5f, 0.5f, 0.5f);
        output.updateBuffer(model);
        blockRenderer.tesselateBlock(output, 0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
        matrices.popPose();

        matrices.pushPose();
        blockState = Blocks.AIR.defaultBlockState();
        world.blockState(blockState);
        model = AllPartialModels.MECHANICAL_PRESS_HEAD.get();
        matrices.translate(0, getAnimatedHeadOffset(time), 0);
        output.updateBuffer(model);
        blockRenderer.tesselateBlock(output, 0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
        matrices.popPose();

        matrices.translate(0, -1.65f, 0);
        blockState = AllBlocks.BASIN.defaultBlockState();
        world.blockState(blockState);
        model = blockStateModelSet.get(blockState);
        output.updateBuffer(model);
        blockRenderer.tesselateBlock(output, 0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
        output.clear();
    }

    private static float getShaftAngle(float time) {
        return time * 4.0f % 360;
    }

    private static float getAnimatedHeadOffset(float time) {
        float cycle = time % 30;
        if (cycle < 10) {
            float progress = cycle / 10;
            return -(progress * progress * progress);
        }
        if (cycle < 15) {
            return -1;
        }
        if (cycle < 20) {
            return -1 + (1 - (20 - cycle) / 5);
        }
        return 0;
    }

    @Override
    protected String getTextureLabel() {
        return "Press Basin";
    }

    @Override
    public Class<PressBasinRenderState> getRenderStateClass() {
        return PressBasinRenderState.class;
    }
}