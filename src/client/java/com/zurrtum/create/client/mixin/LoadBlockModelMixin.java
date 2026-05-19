package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.Reader;

@Mixin(ModelManager.class)
public class LoadBlockModelMixin {
    @Inject(method = "lambda$loadBlockModels$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/cuboid/CuboidModel;fromStream(Ljava/io/Reader;)Lnet/minecraft/client/resources/model/cuboid/CuboidModel;"), cancellable = true)
    private static void deserialize(
        CallbackInfoReturnable<Pair<Identifier, UnbakedModel>> cir,
        @Local Identifier modelId,
        @Local Reader reader
    ) {
        try {
            UnbakedModel model = GsonHelper.fromJson(CuboidModel.GSON, reader, UnbakedModel.class);
            cir.setReturnValue(Pair.of(modelId, model));
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignore) {
                }
            }
        }
    }
}
