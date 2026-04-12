package com.zurrtum.create.client.content.trains.bogey;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;

public class SmallBogeyRenderState extends StandardBogeyRenderState {
    public @UnknownNullability SuperByteBufferRenderState frame;
    public @UnknownNullability SuperByteBufferRenderState wheels;
    public @Nullable Quaternionf wheelAngle;

    @Override
    public void submit(PoseStack matrices, SubmitNodeCollector queue) {
        super.submit(matrices, queue);
        matrices.pushPose();
        matrices.scale(0.998046875f, 0.998046875f, 0.998046875f);
        frame.submit(matrices, queue);
        matrices.popPose();
        matrices.pushPose();
        if (wheelAngle != null) {
            matrices.last().normal().rotate(wheelAngle);
            matrices.pushPose();
            matrices.translate(0, 0.75f, 1);
            matrices.last().pose().rotate(wheelAngle);
            wheels.submit(matrices, queue);
            matrices.popPose();
            matrices.pushPose();
            matrices.translate(0, 0.75f, -1);
            matrices.last().pose().rotate(wheelAngle);
            wheels.submit(matrices, queue);
            matrices.popPose();
        } else {
            matrices.translate(0, 0.75f, 1);
            wheels.submit(matrices, queue);
            matrices.translate(0, 0, -2);
            wheels.submit(matrices, queue);
        }
        matrices.popPose();
    }
}
