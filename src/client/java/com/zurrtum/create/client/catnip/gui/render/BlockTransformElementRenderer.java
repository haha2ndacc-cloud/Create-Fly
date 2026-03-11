package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneTorchBlock;

import java.util.HashMap;
import java.util.Map;

public class BlockTransformElementRenderer extends PictureInPictureRenderer<BlockTransformRenderState> {
    private static final Map<Object, GpuTexture> TEXTURES = new HashMap<>();
    private final PoseStack matrices = new PoseStack();
    private final BlockBakedQuadOutput output;
    private final TerrainBakedQuadOutput terrainOutput;
    private final ModelBlockRenderer blockRenderer;
    private int windowScaleFactor;

    public BlockTransformElementRenderer(BufferSource vertexConsumers) {
        super(vertexConsumers);
        output = new BlockBakedQuadOutput(vertexConsumers, matrices);
        terrainOutput = new TerrainBakedQuadOutput(vertexConsumers, matrices);
        Minecraft minecraft = Minecraft.getInstance();
        boolean ambientOcclusion = minecraft.options.ambientOcclusion().get();
        blockRenderer = new ModelBlockRenderer(ambientOcclusion, false, minecraft.getBlockColors());
    }

    public static void clear(Object key) {
        GpuTexture texture = TEXTURES.remove(key);
        if (texture != null) {
            texture.close();
        }
    }

    @Override
    public void prepare(BlockTransformRenderState block, GuiRenderState state, int windowScaleFactor) {
        if (this.windowScaleFactor != windowScaleFactor) {
            this.windowScaleFactor = windowScaleFactor;
            TEXTURES.values().forEach(GpuTexture::close);
            TEXTURES.clear();
        }
        BlockTransformRenderKey key = block.key();
        GpuTexture texture = TEXTURES.get(key);
        if (texture == null || key.dirty) {
            float size = key.size * windowScaleFactor;
            if (key.dirty) {
                key.dirty = false;
                if (texture != null && texture.width() != size) {
                    texture.close();
                    texture = null;
                }
            }
            if (texture == null) {
                texture = GpuTexture.create((int) size);
                TEXTURES.put(key, texture);
            }
            texture.prepare(projection, projectionMatrixBuffer);
            matrices.pushPose();
            matrices.translate(size / 2, size / 2, 0);
            if (key.padding != 0) {
                size -= key.padding * windowScaleFactor;
            }
            matrices.scale(size, size, size);
            if (key.zRot != 0) {
                matrices.mulPose(Axis.ZP.rotation(key.zRot));
            }
            if (key.xRot != 0) {
                matrices.mulPose(Axis.XP.rotation(key.xRot));
            }
            if (key.yRot != 0) {
                matrices.mulPose(Axis.YP.rotation(key.yRot));
            }
            matrices.scale(1, -1, 1);
            matrices.translate(-0.5F, -0.5F, -0.5F);
            SinglePosVirtualBlockGetter world = SinglePosVirtualBlockGetter.createFullDark();
            world.blockState(key.state);
            if (key.state.is(Blocks.REDSTONE_TORCH) && key.state.getValue(RedstoneTorchBlock.LIT)) {
                blockRenderer.tesselateBlock(terrainOutput, 0, 0, 0, world, BlockPos.ZERO, key.state, key.model, 42L);
            } else {
                output.updateBuffer(key.model);
                blockRenderer.tesselateBlock(output, 0, 0, 0, world, BlockPos.ZERO, key.state, key.model, 42L);
                output.clearBuffer();
            }
            bufferSource.endBatch();
            matrices.popPose();
            texture.clear();
        }
        state.addBlitToCurrentLayer(new BlitRenderState(
            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
            TextureSetup.singleTexture(texture.textureView(),
                RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)
            ),
            block.pose(),
            block.x0(),
            block.y0(),
            block.x1(),
            block.y1(),
            0.0F,
            1.0F,
            1.0F,
            0.0F,
            -1,
            block.scissorArea(),
            null
        ));
    }

    @Override
    protected void renderToTexture(BlockTransformRenderState block, PoseStack matrices) {
    }

    @Override
    protected String getTextureLabel() {
        return "Block Transform";
    }

    @Override
    public Class<BlockTransformRenderState> getRenderStateClass() {
        return BlockTransformRenderState.class;
    }
}
