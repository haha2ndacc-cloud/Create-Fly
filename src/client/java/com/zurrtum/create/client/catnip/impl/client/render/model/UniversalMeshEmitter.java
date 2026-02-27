package com.zurrtum.create.client.catnip.impl.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.catnip.client.render.model.ShadeSeparatedBufferSource;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.jetbrains.annotations.UnknownNullability;

// Modified from https://github.com/Engine-Room/Flywheel/blob/2f67f54c8898d91a48126c3c753eefa6cd224f84/forge/src/lib/java/dev/engine_room/flywheel/lib/model/baked/MeshEmitter.java
public class UniversalMeshEmitter implements VertexConsumer, BlockQuadOutput {
    @UnknownNullability
    private ShadeSeparatedBufferSource bufferSource;
    @UnknownNullability
    private PoseStack poseStack;

    public void prepare(ShadeSeparatedBufferSource bufferSource, PoseStack poseStack) {
        this.bufferSource = bufferSource;
        this.poseStack = poseStack;
    }

    public void clear() {
        bufferSource = null;
        poseStack = null;
    }

    @Override
    public void putBlockBakedQuad(float x, float y, float z, BakedQuad quad, QuadInstance instance) {
        bufferSource.getBuffer(quad.spriteInfo().layer(), quad.shade()).putBlockBakedQuad(x, y, z, quad, instance);
    }

    @Override
    public void putBakedQuad(PoseStack.Pose pose, BakedQuad quad, QuadInstance instance) {
        bufferSource.getBuffer(quad.spriteInfo().layer(), quad.shade()).putBakedQuad(pose, quad, instance);
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
