package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.PoseStack;

public interface BufferPoseEmitter extends BufferEmitter {
    PoseStack.Pose getPose();
}
