package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector.CustomGeometryRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;

public interface SuperByteBufferRenderState extends CustomGeometryRenderer {
    void submit(PoseStack matrices, OrderedSubmitNodeCollector queue);

    void submit(Pose transform, PoseStack matrices, OrderedSubmitNodeCollector queue);

    void renderInto(Pose pose, VertexConsumer consumer);

    void submit(RenderType type, PoseStack matrices, OrderedSubmitNodeCollector queue);

    void recycle();

    default boolean isEmpty() {
        return false;
    }
}
