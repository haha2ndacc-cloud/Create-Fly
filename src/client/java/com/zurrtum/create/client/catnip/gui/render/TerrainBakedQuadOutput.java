package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.jetbrains.annotations.UnknownNullability;

public class TerrainBakedQuadOutput implements BlockQuadOutput {
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
    public void put(float x, float y, float z, BakedQuad quad, QuadInstance instance) {
        RenderType renderType = switch (quad.spriteInfo().layer()) {
            case SOLID -> RenderTypes.solidMovingBlock();
            case CUTOUT -> RenderTypes.cutoutMovingBlock();
            case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
        };
        if (x != 0F || y != 0F || z != 0F) {
            poseStack.pushPose();
            poseStack.translate(x, y, z);
            bufferSource.getBuffer(renderType).putBakedQuad(poseStack.last(), quad, instance);
            poseStack.popPose();
        } else {
            bufferSource.getBuffer(renderType).putBakedQuad(poseStack.last(), quad, instance);
        }
    }
}
