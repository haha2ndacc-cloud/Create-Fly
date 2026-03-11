package com.zurrtum.create.client.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.AllFluidConfigs;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(FluidStateModelSet.class)
public class FluidStateModelSetMixin {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @WrapOperation(method = "bake(Lnet/minecraft/client/resources/model/sprite/MaterialBaker;)Ljava/util/Map;", at = @At(value = "INVOKE", target = "Ljava/util/Map;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/Map;"))
    private static <K, V> Map<K, V> bake(
        K k1,
        V v1,
        K k2,
        V v2,
        K k3,
        V v3,
        K k4,
        V v4,
        Operation<Map<K, V>> original,
        @Local(argsOnly = true) MaterialBaker materials
    ) {
        ImmutableMap.Builder builder = ImmutableMap.builder();
        builder.put(k1, v1);
        builder.put(k2, v2);
        builder.put(k3, v3);
        builder.put(k4, v4);
        AllFluidConfigs.MODEL.forEach((fluid, unbaked) -> {
            FluidModel model = unbaked.bake(materials, () -> BuiltInRegistries.FLUID.getKey(fluid).toString());
            builder.put(fluid, model);
            builder.put(fluid.getFlowing(), model);
        });
        return builder.build();
    }
}
