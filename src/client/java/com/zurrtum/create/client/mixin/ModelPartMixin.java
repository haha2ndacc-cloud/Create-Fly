package com.zurrtum.create.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.DualVertexConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.ItemMeshEmitter;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPart.class)
public class ModelPartMixin {
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At("HEAD"), cancellable = true)
    private void render(
        PoseStack poseStack,
        VertexConsumer buffer,
        int lightCoords,
        int overlayCoords,
        int color,
        CallbackInfo ci
    ) {
        if (buffer instanceof SpriteCoordinateExpander consumer) {
            VertexConsumer delegate = consumer.delegate;
            if (delegate instanceof ItemMeshEmitter emitter) {
                emitter.emit(
                    (ModelPart) (Object) this,
                    poseStack,
                    consumer.sprite,
                    null,
                    lightCoords,
                    overlayCoords,
                    color
                );
                ci.cancel();
            } else if (delegate instanceof DualVertexConsumer dual) {
                dual.emit((ModelPart) (Object) this, poseStack, consumer.sprite, lightCoords, overlayCoords, color);
                ci.cancel();
            }
        }
    }
}
