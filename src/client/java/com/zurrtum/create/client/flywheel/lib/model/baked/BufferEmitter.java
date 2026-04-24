package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public interface BufferEmitter extends BufferEmitterOutput {
    VertexConsumer getBuffer(boolean shade, ChunkSectionLayer layer);
}
