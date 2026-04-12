package com.zurrtum.create.client.content.trains.bogey;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityRenderer.BogeyRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public class StandardBogeyRenderState implements BogeyRenderState {
    public @UnknownNullability SuperByteBufferRenderState shaft;
    public @Nullable Quaternionf angle;
    public double offset;

    @Override
    public void submit(PoseStack matrices, SubmitNodeCollector queue) {
        matrices.translate(0, offset, 0);
        matrices.pushPose();
        if (angle != null) {
            matrices.last().normal().rotate(angle);
            matrices.pushPose();
            matrices.translate(-0.5f, 0.25f, 0);
            matrices.last().pose().rotateAround(angle, 0.5f, 0.5f, 0.5f);
            shaft.submit(matrices, queue);
            matrices.popPose();
            matrices.pushPose();
            matrices.translate(-0.5f, 0.25f, -1);
            matrices.last().pose().rotateAround(angle, 0.5f, 0.5f, 0.5f);
            shaft.submit(matrices, queue);
            matrices.popPose();
        } else {
            matrices.translate(-0.5f, 0.25f, 0);
            shaft.submit(matrices, queue);
            matrices.translate(0, 0, -1);
            shaft.submit(matrices, queue);
        }
        matrices.popPose();
    }
}
