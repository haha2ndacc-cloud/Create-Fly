package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.model.NormalsBakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VertexConsumer.class)
public interface VertexConsumerMixin {
    @Inject(method = "putBlockBakedQuad(FFFLnet/minecraft/client/resources/model/geometry/BakedQuad;Lcom/mojang/blaze3d/vertex/QuadInstance;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/QuadInstance;getLightCoordsWithEmission(II)I"))
    private void applyBakedNormals(
        float x,
        float y,
        float z,
        BakedQuad quad,
        QuadInstance instance,
        CallbackInfo ci,
        @Local(name = "normal") LocalRef<Vector3fc> generated,
        @Local(name = "vertex") int vertex
    ) {
        int[] normals = NormalsBakedQuad.getNormals(quad);
        if (normals != null) {
            int value = normals[vertex];
            if (value != 0) {
                byte nx = (byte) (value & 0xFF);
                byte ny = (byte) ((value >> 8) & 0xFF);
                byte nz = (byte) ((value >> 16) & 0xFF);
                generated.set(new Vector3f(nx / 127f, ny / 127f, nz / 127f));
            }
        }
    }

    @Inject(method = "putBakedQuad(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/resources/model/geometry/BakedQuad;Lcom/mojang/blaze3d/vertex/QuadInstance;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/QuadInstance;getLightCoordsWithEmission(II)I"))
    private void applyBakedNormals(
        PoseStack.Pose pose,
        BakedQuad quad,
        QuadInstance instance,
        CallbackInfo ci,
        @Local(name = "normal") Vector3f normal,
        @Local(name = "vertex") int vertex
    ) {
        int[] normals = NormalsBakedQuad.getNormals(quad);
        if (normals != null) {
            int value = normals[vertex];
            if (value != 0) {
                byte nx = (byte) (value & 0xFF);
                byte ny = (byte) ((value >> 8) & 0xFF);
                byte nz = (byte) ((value >> 16) & 0xFF);
                normal.set(nx / 127f, ny / 127f, nz / 127f);
                normal.mul(pose.normal());
            }
        }
    }
}