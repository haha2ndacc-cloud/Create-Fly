package com.zurrtum.create.client.mixin;

import com.mojang.blaze3d.platform.Lighting;
import com.zurrtum.create.client.flywheel.backend.engine.uniform.LevelUniforms;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Lighting.class)
public class LightingMixin {
    @Inject(method = "updateBuffer(Lcom/mojang/blaze3d/platform/Lighting$Entry;Lorg/joml/Vector3fc;Lorg/joml/Vector3fc;)V", at = @At("TAIL"))
    private void updateBuffer(
        Lighting.Entry type,
        Vector3fc light0Diffusion,
        Vector3fc light1Diffusion,
        CallbackInfo ci
    ) {
        LevelUniforms.update(type, light0Diffusion, light1Diffusion);
    }

    @Inject(method = "setupFor(Lcom/mojang/blaze3d/platform/Lighting$Entry;)V", at = @At("TAIL"))
    private void setShaderLights(Lighting.Entry type, CallbackInfo ci) {
        LevelUniforms.set(type);
    }
}
