package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

public record EntityBlockMultipleLayer(SuperByteBufferRenderState[] layers) implements SuperByteBufferRenderState {
    @Override
    public void submit(PoseStack matrices, OrderedSubmitNodeCollector queue) {
        for (SuperByteBufferRenderState layer : layers) {
            layer.submit(matrices, queue);
        }
    }

    @Override
    public void submit(Pose transform, PoseStack matrices, OrderedSubmitNodeCollector queue) {
        matrices.pushPose();
        SuperByteBuffer.mul(matrices.last(), transform);
        for (SuperByteBufferRenderState layer : layers) {
            layer.submit(matrices, queue);
        }
        matrices.popPose();
    }

    @Override
    public void submit(RenderType type, PoseStack matrices, OrderedSubmitNodeCollector queue) {
        for (SuperByteBufferRenderState layer : layers) {
            layer.submit(type, matrices, queue);
        }
    }

    @Override
    public void renderInto(Pose pose, VertexConsumer consumer) {
        for (SuperByteBufferRenderState layer : layers) {
            layer.renderInto(pose, consumer);
        }
    }

    @Override
    public void render(Pose pose, VertexConsumer buffer) {
        for (SuperByteBufferRenderState layer : layers) {
            layer.render(pose, buffer);
        }
    }
}
