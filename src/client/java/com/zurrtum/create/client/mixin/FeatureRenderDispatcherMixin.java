package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.catnip.render.EntityBlockLayer;
import com.zurrtum.create.client.catnip.render.EntityBlockLightLayer;
import com.zurrtum.create.client.catnip.render.EntityBlockMultipleLayer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FeatureRenderDispatcher.class)
public class FeatureRenderDispatcherMixin {
    @Inject(method = "clearSubmitNodes()V", at = @At("TAIL"))
    private void onClear(CallbackInfo ci) {
        EntityBlockLightLayer.recycleAll();
        EntityBlockLayer.recycleAll();
        EntityBlockMultipleLayer.recycleAll();
    }
}
