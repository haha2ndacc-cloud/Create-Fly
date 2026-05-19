package com.zurrtum.create.client.flywheel.impl.event;

import net.minecraft.client.multiplayer.ClientLevel;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

public interface RenderContextHolder {
    void flywheel$updateRenderContext(@Nullable ClientLevel level, float partialTick);

    void flywheel$updateProjection(Matrix4fc projection);
}
