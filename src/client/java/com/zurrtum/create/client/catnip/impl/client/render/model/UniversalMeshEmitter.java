package com.zurrtum.create.client.catnip.impl.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadBrightness;
import com.mojang.blaze3d.vertex.QuadLightmapCoords;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.catnip.client.render.model.ShadeSeparatedBufferSource;
import net.minecraft.client.renderer.block.BakedQuadOutput;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.jetbrains.annotations.UnknownNullability;

// Modified from https://github.com/Engine-Room/Flywheel/blob/2f67f54c8898d91a48126c3c753eefa6cd224f84/forge/src/lib/java/dev/engine_room/flywheel/lib/model/baked/MeshEmitter.java
public class UniversalMeshEmitter implements VertexConsumer, BakedQuadOutput {
    @UnknownNullability
    private ShadeSeparatedBufferSource bufferSource;

    public void prepare(ShadeSeparatedBufferSource bufferSource) {
        this.bufferSource = bufferSource;
    }

    public void clear() {
        bufferSource = null;
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
        bufferSource.getBuffer(quad.spriteInfo().layer(), quad.shade()).putBulkData(
            pose,
            quad,
            brightness,
            color,
            lightmapCoord,
            overlayCoords
        );
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
        throw new UnsupportedOperationException("UniversalMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setColor(int color) {
        throw new UnsupportedOperationException("UniversalMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        throw new UnsupportedOperationException("UniversalMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        throw new UnsupportedOperationException("UniversalMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        throw new UnsupportedOperationException("UniversalMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        throw new UnsupportedOperationException("UniversalMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        throw new UnsupportedOperationException("UniversalMeshEmitter only supports putBulkData!");
    }

    @Override
    public VertexConsumer setLineWidth(float width) {
        throw new UnsupportedOperationException("UniversalMeshEmitter only supports putBulkData!");
    }
}
