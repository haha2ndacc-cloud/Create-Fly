package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

import java.util.IdentityHashMap;
import java.util.Map;

public class PartialElementRenderer extends PictureInPictureRenderer<PartialRenderState> {
    private static final Map<PartialRenderState, GpuTexture> TEXTURES = new IdentityHashMap<>();
    private final PoseStack matrices = new PoseStack();
    private final BlockBakedQuadOutput output;
    private final ModelBlockRenderer blockRenderer;
    private int windowScaleFactor;

    public PartialElementRenderer(BufferSource vertexConsumers) {
        super(vertexConsumers);
        output = new BlockBakedQuadOutput(vertexConsumers, matrices);
        Minecraft minecraft = Minecraft.getInstance();
        boolean ambientOcclusion = minecraft.options.ambientOcclusion().get();
        blockRenderer = new ModelBlockRenderer(ambientOcclusion, false, minecraft.getBlockColors());
    }

    public static void clear(PartialRenderState block) {
        GpuTexture texture = TEXTURES.remove(block);
        if (texture != null) {
            texture.close();
        }
    }

    @Override
    public void prepare(PartialRenderState partial, GuiRenderState state, int windowScaleFactor) {
        if (partial.model == null) {
            return;
        }
        if (this.windowScaleFactor != windowScaleFactor) {
            this.windowScaleFactor = windowScaleFactor;
            TEXTURES.values().forEach(GpuTexture::close);
            TEXTURES.clear();
        }
        GpuTexture texture = TEXTURES.get(partial);
        boolean draw = texture == null || partial.dirty;
        if (draw) {
            float size = partial.size * windowScaleFactor;
            if (partial.dirty) {
                partial.clearDirty();
                if (texture != null && texture.width() != size) {
                    texture.close();
                    texture = null;
                }
            }
            if (texture == null) {
                texture = GpuTexture.create((int) size);
                TEXTURES.put(partial, texture);
            }
            texture.prepare(projection, projectionMatrixBuffer);
            matrices.pushPose();
            if (partial.padding != 0) {
                size -= partial.padding * windowScaleFactor;
            }
            matrices.scale(size, size, size);
            partial.transform(matrices);
            SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullDark();
            output.updateBuffer(partial.model);
            blockRenderer.tesselateBlock(
                output,
                0,
                0,
                0,
                world,
                BlockPos.ZERO,
                Blocks.AIR.defaultBlockState(),
                partial.model,
                42L
            );
            output.clearBuffer();
            bufferSource.endBatch();
            matrices.popPose();
            texture.clear();
        }
        state.submitBlitToCurrentLayer(new BlitRenderState(
            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
            TextureSetup.singleTexture(
                texture.textureView(),
                RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)
            ),
            partial.pose,
            partial.x1,
            partial.y1,
            partial.x2,
            partial.y2,
            0.0F,
            1.0F,
            1.0F,
            0.0F,
            -1,
            partial.scissor,
            null
        ));
    }

    @Override
    protected void renderToTexture(PartialRenderState partial, PoseStack matrices) {
    }

    @Override
    protected String getTextureLabel() {
        return "Partial";
    }

    @Override
    public Class<PartialRenderState> getRenderStateClass() {
        return PartialRenderState.class;
    }
}
