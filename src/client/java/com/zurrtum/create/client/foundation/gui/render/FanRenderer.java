package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Lighting.Entry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.render.BlockBakedQuadOutput;
import com.zurrtum.create.client.catnip.gui.render.TerrainBakedQuadOutput;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class FanRenderer extends PictureInPictureRenderer<FanRenderState> {
    private final BlockBakedQuadOutput output;
    private final TerrainBakedQuadOutput terrainOutput;
    private final ModelBlockRenderer blockRenderer;

    public FanRenderer(BufferSource vertexConsumers) {
        super(vertexConsumers);
        output = new BlockBakedQuadOutput(vertexConsumers);
        terrainOutput = new TerrainBakedQuadOutput(vertexConsumers);
        Minecraft minecraft = Minecraft.getInstance();
        boolean ambientOcclusion = minecraft.options.ambientOcclusion().get();
        blockRenderer = new ModelBlockRenderer(ambientOcclusion, false, minecraft.getBlockColors());
    }

    @Override
    protected void renderToTexture(FanRenderState state, PoseStack matrices) {
        Minecraft mc = Minecraft.getInstance();
        mc.gameRenderer.getLighting().setupFor(Entry.ENTITY_IN_UI);
        matrices.scale(1, 1, -1);
        matrices.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrices.mulPose(Axis.YP.rotationDegrees(22.5f));
        matrices.translate(-0.92f, -0.75f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockState blockState;
        BlockStateModel model;
        output.setPoseStack(matrices);
        BlockStateModelSet blockStateModelSet = mc.getModelManager().getBlockStateModelSet();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();

        matrices.pushPose();
        blockState = Blocks.AIR.defaultBlockState();
        model = AllPartialModels.ENCASED_FAN_INNER.get();
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.mulPose(Axis.ZP.rotationDegrees(getCurrentAngle() * 16));
        matrices.mulPose(Axis.XP.rotationDegrees(180));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        output.updateBuffer(model);
        blockRenderer.tesselateBlock(output, 0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
        matrices.popPose();

        matrices.pushPose();
        blockState = AllBlocks.ENCASED_FAN.defaultBlockState();
        world.blockState(blockState);
        model = blockStateModelSet.get(blockState);
        matrices.rotateAround(Axis.YP.rotationDegrees(180), 0.5f, 0.5f, 0.5f);
        output.updateBuffer(model);
        blockRenderer.tesselateBlock(output, 0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
        matrices.popPose();

        matrices.translate(0, 0, 2);
        blockState = state.target();
        FluidState fluidState = blockState.getFluidState();
        if (!fluidState.isEmpty()) {
            Fluid fluid = fluidState.getType();
            //            SodiumCompat.markFluidSpriteActive(fluid);
            FluidStateModelSet fluidStateModelSet = mc.getModelManager().getFluidStateModelSet();
            FluidRenderHelper.extractFluidRenderState(
                null,
                null,
                fluidStateModelSet,
                fluid,
                DataComponentPatch.EMPTY,
                0,
                0,
                0,
                1,
                1,
                1,
                LightCoordsUtil.FULL_BRIGHT,
                false,
                true
            ).render(matrices, bufferSource);
            return;
        }
        world.blockState(blockState);
        model = blockStateModelSet.get(blockState);
        if (blockState.getBlock() instanceof BaseFireBlock) {
            terrainOutput.setPoseStack(matrices);
            blockRenderer.tesselateBlock(terrainOutput, 0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
            terrainOutput.clear();
        } else {
            output.updateBuffer(model);
            blockRenderer.tesselateBlock(output, 0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
        }
        output.clear();
    }

    public static float getCurrentAngle() {
        return AnimationTickHolder.getRenderTime() * 4.0f % 360;
    }

    @Override
    protected String getTextureLabel() {
        return "Fan";
    }

    @Override
    public Class<FanRenderState> getRenderStateClass() {
        return FanRenderState.class;
    }
}
