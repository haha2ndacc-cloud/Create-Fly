package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.renderer.block.BakedQuadOutput;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

@Deprecated(forRemoval = true)
public class ShadedBlockSbbBuilder implements VertexConsumer, BakedQuadOutput {
    protected static final ByteBufferBuilder BYTE_BUFFER_BUILDER = new ByteBufferBuilder(512);
    protected BufferBuilder bufferBuilder;
    protected final IntList shadeSwapVertices = new IntArrayList();
    protected boolean currentShade;
    protected boolean invertFakeNormal;
    protected ChunkSectionLayer filter;

    public static ShadedBlockSbbBuilder create() {
        return new ShadedBlockSbbBuilder();
    }

    public static ShadedBlockSbbBuilder createForPonder() {
        ShadedBlockSbbBuilder builder = new ShadedBlockSbbBuilder();
        builder.invertFakeNormal = true;
        return builder;
    }

    public void begin(ChunkSectionLayer layer) {
        filter = layer;
        bufferBuilder = new BufferBuilder(BYTE_BUFFER_BUILDER, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
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
    public void putBulkData(
        PoseStack.Pose pose,
        BakedQuad quad,
        QuadBrightness brightness,
        int color,
        QuadLightmapCoords lightmapCoord,
        int overlayCoords
    ) {
        if (quad.spriteInfo().layer() == filter) {
            prepareForGeometry(quad);
            bufferBuilder.putBulkData(pose, quad, brightness, color, lightmapCoord, overlayCoords);
        }
    }

    @Override
    public void put(
        PoseStack.Pose pose,
        BakedQuad quad,
        QuadBrightness brightness,
        int color,
        QuadLightmapCoords lightmapCoord,
        int overlayCoords
    ) {
        putBulkData(pose, quad, brightness, color, lightmapCoord, overlayCoords);
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
