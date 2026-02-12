package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadBrightness;
import com.mojang.blaze3d.vertex.QuadLightmapCoords;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.model.NormalsBakedQuad;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VertexConsumer.class)
public interface VertexConsumerMixin {
    @Inject(method = "putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;Lcom/mojang/blaze3d/vertex/QuadBrightness;ILcom/mojang/blaze3d/vertex/QuadLightmapCoords;I)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/QuadLightmapCoords;composeWithEmission(II)I"))
    private void applyBakedNormals(
        PoseStack.Pose pose,
        BakedQuad quad,
        QuadBrightness brightness,
        int color,
        QuadLightmapCoords lightmapCoord,
        int overlayCoords,
        CallbackInfo ci,
        @Local Vector3f generated,
        @Local(ordinal = 3) int vertex
    ) {
        int[] normals = NormalsBakedQuad.getNormals(quad);
        if (normals != null) {
            int normal = normals[vertex];
            if (normal != 0) {
                byte nx = (byte) (normal & 0xFF);
                byte ny = (byte) ((normal >> 8) & 0xFF);
                byte nz = (byte) ((normal >> 16) & 0xFF);
                generated.set(nx / 127f, ny / 127f, nz / 127f);
                generated.mul(pose.normal());
            }
        }
    }
}