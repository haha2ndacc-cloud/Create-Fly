package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.catnip.render.CustomRenderType;
import com.zurrtum.create.client.foundation.render.CustomRenderPipeline;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderType.class)
public class RenderTypeMixin implements CustomRenderType {
    @Shadow
    @Final
    public RenderSetup state;
    @Unique
    private boolean solidBlend;
    @Unique
    private boolean priority;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(String name, RenderSetup state, CallbackInfo ci) {
        solidBlend = ((CustomRenderPipeline) state.pipeline).create$isSolidBlend();
    }

    @Inject(method = "hasBlending()Z", at = @At("HEAD"), cancellable = true)
    private void skipBlendCheck(CallbackInfoReturnable<Boolean> cir) {
        if (solidBlend) {
            cir.setReturnValue(false);
        }
    }

    @Override
    public boolean create$isPriority() {
        return priority;
    }

    @Override
    public void create$markPriority() {
        priority = true;
    }
}
