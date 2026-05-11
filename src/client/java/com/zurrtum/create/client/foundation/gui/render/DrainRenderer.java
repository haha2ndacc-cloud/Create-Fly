package com.zurrtum.create.client.foundation.gui.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.catnip.gui.render.BlockBakedQuadOutput;
import com.zurrtum.create.client.catnip.gui.render.GpuTexture;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelRenderHelper;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.Deque;

public class DrainRenderer extends PictureInPictureRenderer<DrainRenderState> {
    public static int MAX = 6;
    private int allocate = MAX;
    private static final Deque<GpuTexture> TEXTURES = new ArrayDeque<>(MAX);
    private final PoseStack matrices = new PoseStack();
    private final BlockBakedQuadOutput output;
    private int windowScaleFactor;

    public DrainRenderer(BufferSource vertexConsumers) {
        super(vertexConsumers);
        output = new BlockBakedQuadOutput(vertexConsumers, matrices);
    }

    @Override
    public void prepare(DrainRenderState element, GuiRenderState state, int windowScaleFactor) {
        if (this.windowScaleFactor != windowScaleFactor) {
            this.windowScaleFactor = windowScaleFactor;
            TEXTURES.forEach(GpuTexture::close);
            TEXTURES.clear();
            allocate = MAX;
        }
        int width = 26 * windowScaleFactor;
        int height = 23 * windowScaleFactor;
        GpuTexture texture;
        if (allocate > 0) {
            allocate--;
            texture = GpuTexture.create(width, height);
        } else {
            texture = TEXTURES.poll();
            assert texture != null;
        }
        texture.prepare(projection, projectionMatrixBuffer);
        matrices.pushPose();
        matrices.translate(width / 2.0F, height, 0.0F);
        float scale = 20 * windowScaleFactor;
        matrices.scale(scale, scale, scale);

        Minecraft mc = Minecraft.getInstance();
        mc.gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        matrices.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrices.mulPose(Axis.YP.rotationDegrees(22.5f));
        matrices.scale(1, -1, 1);
        matrices.translate(-0.5f, 0.2f, -0.5f);

        SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullBright();

        BlockState blockState = AllBlocks.ITEM_DRAIN.defaultBlockState();
        world.blockState(blockState);
        BlockStateModel model = mc.getModelManager().getBlockStateModelSet().get(blockState);
        output.updateBuffer(model);
        ModelRenderHelper.getHelper(output).tesselateBlock(0, 0, 0, world, BlockPos.ZERO, blockState, model, 42L);
        output.clearBuffer();

        float from = 2 / 16f;
        float to = 1f - from;
        FluidStateModelSet fluidStateModelSet = mc.getModelManager().getFluidStateModelSet();
        FluidRenderHelper.extractFluidRenderState(
            null,
            null,
            fluidStateModelSet,
            element.fluid(),
            element.components(),
            from,
            from,
            from,
            to,
            3 / 4f,
            to,
            LightCoordsUtil.FULL_BRIGHT,
            false,
            true
        ).render(matrices, bufferSource);

        bufferSource.endBatch();
        matrices.popPose();
        texture.clear();
        state.addBlitToCurrentLayer(new BlitRenderState(
            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
            TextureSetup.singleTexture(texture.textureView(),
                RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)
            ),
            element.pose(),
            element.x0(),
            element.y0(),
            element.x1(),
            element.y1(),
            0.0F,
            1.0F,
            1.0F,
            0.0F,
            -1,
            null,
            null
        ));
        TEXTURES.add(texture);
    }

    @Override
    protected void renderToTexture(DrainRenderState state, PoseStack matrices) {
    }

    @Override
    protected String getTextureLabel() {
        return "Drain";
    }

    @Override
    public Class<DrainRenderState> getRenderStateClass() {
        return DrainRenderState.class;
    }
}
