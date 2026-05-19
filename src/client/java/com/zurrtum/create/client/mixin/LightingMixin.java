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
    private void updateBuffer(Lighting.Entry entry, Vector3fc light0, Vector3fc light1, CallbackInfo ci) {
        LevelUniforms.update(entry, light0, light1);
    }

    @Inject(method = "setupFor(Lcom/mojang/blaze3d/platform/Lighting$Entry;)V", at = @At("TAIL"))
    private void setShaderLights(Lighting.Entry type, CallbackInfo ci) {
        LevelUniforms.set(type);
    }
}
