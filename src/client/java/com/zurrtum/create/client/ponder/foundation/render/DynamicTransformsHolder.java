package com.zurrtum.create.client.ponder.foundation.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import org.jspecify.annotations.Nullable;

public interface DynamicTransformsHolder {
    void ponder$updateTransforms(@Nullable GpuBufferSlice dynamicTransforms);
}
