package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.model.NormalsBakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(VertexConsumer.class)
public interface VertexConsumerMixin {
    @ModifyVariable(method = "putBlockBakedQuad(FFFLnet/minecraft/client/resources/model/geometry/BakedQuad;Lcom/mojang/blaze3d/vertex/QuadInstance;)V", at = @At(value = "INVOKE", target = "Lorg/joml/Vector3fc;x()F", ordinal = 0), name = "normal")
    private Vector3fc applyBakedNormals(
        Vector3fc origin,
        @Local(argsOnly = true) BakedQuad quad,
        @Local(name = "vertex") int vertex
    ) {
        Vector3fc normal = ((NormalsBakedQuad) (Object) quad).create$getNormal(vertex);
        return normal != null ? normal : origin;
    }

    @ModifyVariable(method = "putBakedQuad(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/resources/model/geometry/BakedQuad;Lcom/mojang/blaze3d/vertex/QuadInstance;)V", at = @At(value = "INVOKE", target = "Lorg/joml/Vector3f;x()F", ordinal = 0), name = "normal")
    private Vector3f applyBakedNormals(
        Vector3f origin,
        @Local(argsOnly = true) PoseStack.Pose pose,
        @Local(argsOnly = true) BakedQuad quad,
        @Local(name = "vertex") int vertex
    ) {
        Vector3fc normal = ((NormalsBakedQuad) (Object) quad).create$getNormal(vertex);
        if (normal != null) {
            pose.transformNormal(normal, origin);
        }
        return origin;
    }
}
