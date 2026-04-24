package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import org.jspecify.annotations.Nullable;

public interface BufferAoPoseEmitter extends BufferEmitterOutput {
    PoseStack.Pose getPose();

    @Nullable VertexConsumer getBuffer(ChunkSectionLayer layer, boolean shade, boolean ambientOcclusion);
}
