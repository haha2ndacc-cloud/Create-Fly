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
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MixingBasinRenderer extends PictureInPictureRenderer<MixingBasinRenderState> {
    private final BlockBakedQuadOutput output;

    public MixingBasinRenderer(BufferSource vertexConsumers) {
        super(vertexConsumers);
        output = new BlockBakedQuadOutput(vertexConsumers);
    }

    @Override
    protected void renderToTexture(MixingBasinRenderState state, PoseStack matrices) {
        Minecraft mc = Minecraft.getInstance();
        mc.gameRenderer.getLighting().setupFor(Entry.ENTITY_IN_UI);
        matrices.scale(1, 1, -1);
        matrices.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrices.mulPose(Axis.YP.rotationDegrees(22.5f));
        matrices.translate(-0.5f, -1.8f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockState blockState;
        BlockStateModel model;
        ModelConsumer blockRenderer = ModelRenderHelper.getHelper(output);
        output.setPoseStack(matrices);
        BlockStateModelSet blockStateModelSet = mc.getModelManager().getBlockStateModelSet();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        float time = AnimationTickHolder.getRenderTime();
        float angle = getCurrentAngle(time);

        blockState = AllBlocks.MECHANICAL_MIXER.defaultBlockState();
        world.blockState(blockState);
        model = blockStateModelSet.get(blockState);
        output.updateBuffer(model);
        blockRenderer.tesselateBlock(0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);

        blockState = Blocks.AIR.defaultBlockState();
        world.blockState(blockState);
        matrices.pushPose();
        model = AllPartialModels.SHAFTLESS_COGWHEEL.get();
        matrices.rotateAround(Axis.YP.rotationDegrees(angle * 2), 0.5f, 0.5f, 0.5f);
        output.updateBuffer(model);
        blockRenderer.tesselateBlock(0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
        matrices.popPose();

        matrices.pushPose();
        matrices.translate(0, getAnimatedHeadOffset(time), 0);
        model = AllPartialModels.MECHANICAL_MIXER_POLE.get();
        output.updateBuffer(model);
        blockRenderer.tesselateBlock(0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
        matrices.rotateAround(Axis.YP.rotationDegrees(angle * 4), 0.5f, 0.5f, 0.5f);
        model = AllPartialModels.MECHANICAL_MIXER_HEAD.get();
        output.updateBuffer(model);
        blockRenderer.tesselateBlock(0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
        matrices.popPose();

        matrices.translate(0, -1.65f, 0);
        blockState = AllBlocks.BASIN.defaultBlockState();
        world.blockState(blockState);
        model = blockStateModelSet.get(blockState);
        output.updateBuffer(model);
        blockRenderer.tesselateBlock(0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
        output.clear();
    }

    private static float getCurrentAngle(float time) {
        return time * 4.0f % 360;
    }

    private static float getAnimatedHeadOffset(float time) {
        return -((Mth.sin(time / 32.0f) + 1) / 5 + 0.5f);
    }

    @Override
    protected String getTextureLabel() {
        return "Mixing Basin";
    }

    @Override
    public Class<MixingBasinRenderState> getRenderStateClass() {
        return MixingBasinRenderState.class;
    }
}