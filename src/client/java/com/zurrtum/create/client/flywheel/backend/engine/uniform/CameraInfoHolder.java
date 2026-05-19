package com.zurrtum.create.client.flywheel.backend.engine.uniform;

import net.minecraft.client.Camera;
import org.joml.Vector3fc;

public interface CameraInfoHolder {
    Vector3fc flywheel$forwardVector();

    void flywheel$update(Camera camera);
}
