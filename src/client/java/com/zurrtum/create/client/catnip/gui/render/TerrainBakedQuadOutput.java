package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.BufferPoseEmitter;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.jetbrains.annotations.UnknownNullability;

public class TerrainBakedQuadOutput implements BlockQuadOutput, BufferPoseEmitter {
    public final MultiBufferSource bufferSource;
    @UnknownNullability
    public PoseStack poseStack;

    public TerrainBakedQuadOutput(MultiBufferSource bufferSource) {
        this.bufferSource = bufferSource;
    }

    public TerrainBakedQuadOutput(MultiBufferSource bufferSource, PoseStack poseStack) {
        this.bufferSource = bufferSource;
        this.poseStack = poseStack;
    }

    public void setPoseStack(PoseStack poseStack) {
        this.poseStack = poseStack;
    }

    public void clear() {
        poseStack = null;
    }

    @Override
    public VertexConsumer getBuffer(boolean shade, ChunkSectionLayer layer) {
        return bufferSource.getBuffer(switch (layer) {
            case SOLID -> RenderTypes.solidMovingBlock();
            case CUTOUT -> RenderTypes.cutoutMovingBlock();
            case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
        });
    }

    @Override
    public void put(float x, float y, float z, BakedQuad quad, QuadInstance instance) {
        VertexConsumer buffer = getBuffer(true, quad.materialInfo().layer());
        if (x != 0F || y != 0F || z != 0F) {
            poseStack.pushPose();
            poseStack.translate(x, y, z);
            buffer.putBakedQuad(poseStack.last(), quad, instance);
            poseStack.popPose();
        } else {
            buffer.putBakedQuad(poseStack.last(), quad, instance);
        }
    }

    @Override
    public PoseStack.Pose getPose() {
        return poseStack.last();
    }
}
