package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.flywheel.backend.engine.uniform.CameraInfoHolder;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CameraRenderState.class)
public class CameraRenderStateMixin implements CameraInfoHolder {
    @Unique
    private final Vector3f forwardVector = new Vector3f();

    @Override
    public @NonNull Vector3fc flywheel$forwardVector() {
        return forwardVector;
    }

    @Override
    public void flywheel$update(@NonNull Camera camera) {
        forwardVector.set(camera.forwardVector());
    }
}
