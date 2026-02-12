package com.zurrtum.create.client.flywheel.impl.event;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public interface RenderContextHolder {
    void flywheel$updateRenderContext(
        Matrix4fc modelView,
        Matrix4f projection,
        Camera camera,
        DeltaTracker deltaTracker
    );

    void flywheel$resetRenderContext();
}
