package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import org.jetbrains.annotations.UnknownNullability;

public class BlockBakedQuadOutput implements BlockQuadOutput {
    public final MultiBufferSource bufferSource;
    @UnknownNullability
    public VertexConsumer buffer;
    @UnknownNullability
    public PoseStack poseStack;

    public BlockBakedQuadOutput(MultiBufferSource bufferSource) {
        this.bufferSource = bufferSource;
    }

    public BlockBakedQuadOutput(MultiBufferSource bufferSource, PoseStack poseStack) {
        this.bufferSource = bufferSource;
        this.poseStack = poseStack;
    }

    public void updateBuffer(BlockStateModel model) {
        buffer = bufferSource.getBuffer(model.hasTranslucency() ? Sheets.translucentBlockSheet() : Sheets.cutoutBlockSheet());
    }

    public void clearBuffer() {
        buffer = null;
    }

    public void setPoseStack(PoseStack poseStack) {
        this.poseStack = poseStack;
    }

    public void clear() {
        buffer = null;
        poseStack = null;
    }

    @Override
    public void put(float x, float y, float z, BakedQuad quad, QuadInstance instance) {
        if (x != 0F || y != 0F || z != 0F) {
            poseStack.pushPose();
            poseStack.translate(x, y, z);
            buffer.putBakedQuad(poseStack.last(), quad, instance);
            poseStack.popPose();
        } else {
            buffer.putBakedQuad(poseStack.last(), quad, instance);
        }
    }
}
