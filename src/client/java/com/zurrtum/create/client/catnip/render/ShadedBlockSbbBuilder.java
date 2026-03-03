package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import org.jetbrains.annotations.UnknownNullability;

@Deprecated(forRemoval = true)
public class ShadedBlockSbbBuilder implements VertexConsumer, BlockQuadOutput {
    protected static final ByteBufferBuilder BYTE_BUFFER_BUILDER = new ByteBufferBuilder(512);
    @UnknownNullability
    protected BufferBuilder bufferBuilder;
    protected final IntList shadeSwapVertices = new IntArrayList();
    protected boolean currentShade;
    protected boolean invertFakeNormal;
    @UnknownNullability
    protected ChunkSectionLayer filter;
    protected PoseStack poseStack;

    public ShadedBlockSbbBuilder(PoseStack poseStack) {
        this.poseStack = poseStack;
    }

    public void begin(ChunkSectionLayer layer) {
        filter = layer;
        bufferBuilder = new BufferBuilder(BYTE_BUFFER_BUILDER, VertexFormat.Mode.QUADS, DefaultVertexFormat.ENTITY);
        shadeSwapVertices.clear();
        currentShade = true;
    }

    public SuperByteBuffer end() {
        MeshData data = bufferBuilder.build();
        TemplateMesh mesh;

        if (data != null) {
            mesh = new MutableTemplateMesh(data).toImmutable();
            data.close();
        } else {
            mesh = new TemplateMesh(0);
        }

        return new ShadeSeparatingSuperByteBuffer(mesh, shadeSwapVertices.toIntArray(), invertFakeNormal);
    }

    public BufferBuilder unwrap(boolean shade) {
        prepareForGeometry(shade);
        return bufferBuilder;
    }

    private void prepareForGeometry(boolean shade) {
        if (shade != currentShade) {
            shadeSwapVertices.add(bufferBuilder.vertices);
            currentShade = shade;
        }
    }

    protected void prepareForGeometry(BakedQuad quad) {
        prepareForGeometry(quad.shade());
    }

    @Override
    public void putBlockBakedQuad(float x, float y, float z, BakedQuad quad, QuadInstance instance) {
        if (quad.spriteInfo().layer() == filter) {
            prepareForGeometry(quad);
            bufferBuilder.putBlockBakedQuad(x, y, z, quad, instance);
        }
    }

    @Override
    public void putBakedQuad(PoseStack.Pose pose, BakedQuad quad, QuadInstance instance) {
        if (quad.spriteInfo().layer() == filter) {
            prepareForGeometry(quad);
            bufferBuilder.putBakedQuad(pose, quad, instance);
        }
    }

    @Override
    public void put(float x, float y, float z, BakedQuad quad, QuadInstance instance) {
        if (x != 0F || y != 0F || z != 0F) {
            poseStack.pushPose();
            poseStack.translate(x, y, z);
            putBakedQuad(poseStack.last(), quad, instance);
            poseStack.popPose();
        } else {
            putBakedQuad(poseStack.last(), quad, instance);
        }
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
    }

    @Override
    public VertexConsumer setColor(int color) {
        throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
    }

    @Override
    public VertexConsumer setLineWidth(float width) {
        throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
    }
}
