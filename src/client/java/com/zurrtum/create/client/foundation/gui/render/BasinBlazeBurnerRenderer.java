package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.platform.Lighting.Entry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.render.BlockBakedQuadOutput;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelRenderHelper;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BasinBlazeBurnerRenderer extends PictureInPictureRenderer<BasinBlazeBurnerRenderState> {
    private final BlockBakedQuadOutput output;

    public BasinBlazeBurnerRenderer(BufferSource vertexConsumers) {
        super(vertexConsumers);
        output = new BlockBakedQuadOutput(vertexConsumers);
    }

    @Override
    protected void renderToTexture(BasinBlazeBurnerRenderState state, PoseStack matrices) {
        Minecraft mc = Minecraft.getInstance();
        GameRenderer gameRenderer = mc.gameRenderer;
        gameRenderer.getLighting().setupFor(Entry.ENTITY_IN_UI);
        matrices.scale(1, 1, -1);
        matrices.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrices.mulPose(Axis.YP.rotationDegrees(22.5f));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockState blockState;
        BlockStateModel model;
        ModelConsumer blockRenderer = ModelRenderHelper.getHelper(output);
        output.setPoseStack(matrices);
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        float offset = -(Mth.sin(AnimationTickHolder.getRenderTime() / 16.0f) + 0.5f) / 16.0f;

        blockState = AllBlocks.BLAZE_BURNER.defaultBlockState();
        world.blockState(blockState);
        model = mc.getModelManager().getBlockStateModelSet().get(blockState);
        output.updateBuffer(model);
        blockRenderer.tesselateBlock(0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);

        matrices.pushPose();
        blockState = Blocks.AIR.defaultBlockState();
        world.blockState(blockState);
        matrices.rotateAround(Axis.YP.rotationDegrees(180), 0.5f, 0.5f, 0.5f);
        boolean seething = state.heat() == HeatLevel.SEETHING;
        model = (seething ? AllPartialModels.BLAZE_SUPER : AllPartialModels.BLAZE_ACTIVE).get();
        output.updateBuffer(model);
        blockRenderer.tesselateBlock(0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
        matrices.translate(0, offset, 0);
        model = (seething ? AllPartialModels.BLAZE_BURNER_SUPER_RODS_2 : AllPartialModels.BLAZE_BURNER_RODS_2).get();
        output.updateBuffer(model);
        blockRenderer.tesselateBlock(0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
        matrices.popPose();
        output.clear();


        SpriteShiftEntry spriteShift = seething ? AllSpriteShifts.SUPER_BURNER_FLAME : AllSpriteShifts.BURNER_FLAME;

        float spriteWidth = spriteShift.getTarget().getU1() - spriteShift.getTarget().getU0();

        float spriteHeight = spriteShift.getTarget().getV1() - spriteShift.getTarget().getV0();

        float time = AnimationTickHolder.getRenderTime(mc.level);
        float speed = 1 / 32.0f + 1 / 64.0f * state.heat().ordinal();

        double vScroll = speed * time;
        vScroll = vScroll - Math.floor(vScroll);
        vScroll = vScroll * spriteHeight / 2;

        double uScroll = speed * time / 2;
        uScroll = uScroll - Math.floor(uScroll);
        uScroll = uScroll * spriteWidth / 2;

        FeatureRenderDispatcher featureRenderDispatcher = gameRenderer.getFeatureRenderDispatcher();
        SubmitNodeStorage queue = featureRenderDispatcher.getSubmitNodeStorage();
        CachedBuffers.partial(AllPartialModels.BLAZE_BURNER_FLAME, Blocks.AIR.defaultBlockState())
            .shiftUVScrolling(spriteShift, (float) uScroll, (float) vScroll).light(LightCoordsUtil.FULL_BRIGHT)
            .submit(matrices, queue);
        featureRenderDispatcher.renderAllFeatures();
    }

    @Override
    protected String getTextureLabel() {
        return "Blaze Burner";
    }

    @Override
    public Class<BasinBlazeBurnerRenderState> getRenderStateClass() {
        return BasinBlazeBurnerRenderState.class;
    }
}
