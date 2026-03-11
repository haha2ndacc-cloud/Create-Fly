package com.zurrtum.create.client.mixin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.client.model.UnbakedModelParser;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CuboidModel.class)
public class CuboidModelMixin {
    @WrapOperation(method = "<clinit>()V", at = @At(value = "INVOKE", target = "Lcom/google/gson/GsonBuilder;create()Lcom/google/gson/Gson;"))
    private static Gson wrap(GsonBuilder instance, Operation<Gson> original) {
        return UnbakedModelParser.wrap(original.call(instance));
    }
}
