package com.zurrtum.create.client.api.behaviour.movement;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;

public interface MovementRenderState {
    default void transform(PoseStack matrices, Pose pose, BlockPos pos) {
        Pose entry = matrices.last();
        SuperByteBuffer.mul(entry, pose);
        entry.translate(pos.getX(), pos.getY(), pos.getZ());
    }

    void submit(PoseStack matrices, SubmitNodeCollector queue);
}
