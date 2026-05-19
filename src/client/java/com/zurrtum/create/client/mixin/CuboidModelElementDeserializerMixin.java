package com.zurrtum.create.client.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.model.NormalsModelElement;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CuboidModelElement.Deserializer.class)
public class CuboidModelElementDeserializerMixin {
    @ModifyReturnValue(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/resources/model/cuboid/CuboidModelElement;", at = @At("RETURN"))
    private CuboidModelElement checkNormals(CuboidModelElement element, @Local JsonObject object) {
        JsonElement data = object.get("neoforge_data");
        if (data != null) {
            try {
                JsonElement value = data.getAsJsonObject().get("calculate_normals");
                if (value != null && value.getAsBoolean()) {
                    ((NormalsModelElement) (Object) element).create$markNormals();
                }
            } catch (Exception ignored) {
            }
        }
        return element;
    }
}
