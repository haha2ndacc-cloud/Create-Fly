package com.zurrtum.create.client.catnip.impl.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.catnip.client.render.model.ShadeSeparatedBufferSource;
import com.zurrtum.create.client.flywheel.lib.model.baked.TransformingVertexConsumer;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.UnknownNullability;

// Modified from https://github.com/Engine-Room/Flywheel/blob/2f67f54c8898d91a48126c3c753eefa6cd224f84/forge/src/lib/java/dev/engine_room/flywheel/lib/model/baked/MeshEmitter.java
public class UniversalMeshEmitter implements VertexConsumer, BlockQuadOutput, FluidRenderer.Output {
    private final TransformingVertexConsumer transformingWrapper = new TransformingVertexConsumer();
    @UnknownNullability
    private ShadeSeparatedBufferSource bufferSource;
    @UnknownNullability
    private PoseStack poseStack;
    @UnknownNullability
    private BlockPos pos;

    public void prepare(ShadeSeparatedBufferSource bufferSource, PoseStack poseStack) {
        this.bufferSource = bufferSource;
        this.poseStack = poseStack;
        transformingWrapper.setPoseStack(poseStack);
    }

    public void clear() {
        bufferSource = null;
        poseStack = null;
        pos = null;
        transformingWrapper.clear();
    }

    @Override
    public void putBlockBakedQuad(float x, float y, float z, BakedQuad quad, QuadInstance instance) {
        MaterialInfo info = quad.materialInfo();
        bufferSource.getBuffer(info.layer(), info.shade()).putBlockBakedQuad(x, y, z, quad, instance);
    }

    @Override
    public void putBakedQuad(PoseStack.Pose pose, BakedQuad quad, QuadInstance instance) {
        MaterialInfo info = quad.materialInfo();
        bufferSource.getBuffer(info.layer(), info.shade()).putBakedQuad(pose, quad, instance);
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

    public void prepareForFluid(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public VertexConsumer getBuilder(ChunkSectionLayer layer) {
        poseStack.translate(
            pos.getX() - (pos.getX() & 0xF),
            pos.getY() - (pos.getY() & 0xF),
            pos.getZ() - (pos.getZ() & 0xF)
        );
        return transformingWrapper.wrap(bufferSource.getBuffer(layer, true));
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
