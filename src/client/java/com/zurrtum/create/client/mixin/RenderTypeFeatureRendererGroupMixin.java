package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.catnip.render.CustomRenderType;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(targets = "net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer$Group")
public class RenderTypeFeatureRendererGroupMixin {
    @WrapOperation(method = "getOrAddDraw(Lnet/minecraft/client/renderer/rendertype/RenderType;)Lnet/minecraft/client/renderer/StagedVertexBuffer$Draw;", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private <E> boolean add(
        List<E> list,
        E e,
        Operation<Boolean> original,
        @Local(argsOnly = true) RenderType renderType
    ) {
        if (((CustomRenderType) renderType).create$isPriority()) {
            list.addFirst(e);
            return true;
        }
        return original.call(list, e);
    }
}
