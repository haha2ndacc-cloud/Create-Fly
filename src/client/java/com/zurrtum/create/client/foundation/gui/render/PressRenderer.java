package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.platform.Lighting.Entry;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.render.BlockBakedQuadOutput;
import com.zurrtum.create.client.catnip.gui.render.GpuTexture;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelRenderHelper;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class PressRenderer extends PictureInPictureRenderer<PressRenderState> {
    private static final Int2ObjectMap<GpuTexture> TEXTURES = new Int2ObjectArrayMap<>();
    private final PoseStack matrices = new PoseStack();
    private final BlockBakedQuadOutput output;
    private int windowScaleFactor;

    public PressRenderer(BufferSource vertexConsumers) {
        super(vertexConsumers);
        output = new BlockBakedQuadOutput(vertexConsumers, matrices);
    }

    @Override
    public void prepare(PressRenderState item, GuiRenderState state, int windowScaleFactor) {
        if (this.windowScaleFactor != windowScaleFactor) {
            this.windowScaleFactor = windowScaleFactor;
            TEXTURES.values().forEach(GpuTexture::close);
            TEXTURES.clear();
        }
        int width = 30 * windowScaleFactor;
        int height = 64 * windowScaleFactor;
        GpuTexture texture = TEXTURES.get(item.id());
        if (texture == null) {
            texture = GpuTexture.create(width, height);
            TEXTURES.put(item.id(), texture);
        }
        texture.prepare(projection, projectionMatrixBuffer);
        matrices.pushPose();
        matrices.translate(width / 2.0F, height, 0.0F);
        float scale = 23 * windowScaleFactor;
        matrices.scale(scale, scale, scale);

        Minecraft mc = Minecraft.getInstance();
        mc.gameRenderer.lighting().setupFor(Entry.ENTITY_IN_UI);
        matrices.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrices.mulPose(Axis.YP.rotationDegrees(22.5f));
        matrices.translate(-0.5f, -1.14f, -0.5f);
        matrices.scale(1, -1, 1);

        BlockState blockState;
        BlockStateModel model;
        BlockStateModelSet blockStateModelSet = mc.getModelManager().getBlockStateModelSet();
        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();
        float time = AnimationTickHolder.getRenderTime();

        blockState = AllBlocks.MECHANICAL_PRESS.defaultBlockState();
        world.blockState(blockState);
        model = blockStateModelSet.get(blockState);
        output.updateBuffer(model);
        ModelConsumer blockRenderer = ModelRenderHelper.getHelper(output);
        blockRenderer.tesselateBlock(0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);

        matrices.pushPose();
        blockState = AllBlocks.SHAFT.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.Z);
        world.blockState(blockState);
        model = blockStateModelSet.get(blockState);
        matrices.rotateAround(Axis.ZP.rotationDegrees(getShaftAngle(time)), 0.5f, 0.5f, 0.5f);
        output.updateBuffer(model);
        blockRenderer.updateOutput(output);
        blockRenderer.tesselateBlock(0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
        matrices.popPose();

        matrices.pushPose();
        blockState = Blocks.AIR.defaultBlockState();
        world.blockState(blockState);
        model = AllPartialModels.MECHANICAL_PRESS_HEAD.get();
        matrices.translate(0, getAnimatedHeadOffset(time, item.offset()), 0);
        output.updateBuffer(model);
        blockRenderer.tesselateBlock(0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
        matrices.popPose();
        output.clearBuffer();

        bufferSource.endBatch();
        matrices.popPose();
        texture.clear();
        state.addBlitToCurrentLayer(new BlitRenderState(
            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
            TextureSetup.singleTexture(
                texture.textureView(),
                RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)
            ),
            item.pose(),
            item.x0(),
            item.y0(),
            item.x1(),
            item.y1(),
            0.0F,
            1.0F,
            1.0F,
            0.0F,
            -1,
            null,
            null
        ));
    }

    private static float getShaftAngle(float time) {
        return time * 4.0f % 360;
    }

    private static float getAnimatedHeadOffset(float time, float offset) {
        float cycle = (time - offset * 8) % 30;
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
    protected void renderToTexture(PressRenderState state, PoseStack matrices) {
    }

    @Override
    protected String getTextureLabel() {
        return "Press";
    }

    @Override
    public Class<PressRenderState> getRenderStateClass() {
        return PressRenderState.class;
    }
}
