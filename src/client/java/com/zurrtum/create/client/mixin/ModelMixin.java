package com.zurrtum.create.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.DualVertexConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.ItemMeshEmitter;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Model.class)
public abstract class ModelMixin {
    @Shadow
    public abstract ModelPart root();

    @Inject(method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At("HEAD"), cancellable = true)
    public void render(
        PoseStack poseStack,
        VertexConsumer buffer,
        int lightCoords,
        int overlayCoords,
        int color,
        CallbackInfo ci
    ) {
        if (buffer instanceof ItemMeshEmitter emitter) {
            emitter.emit(root(), poseStack, null, null, lightCoords, overlayCoords, color);
            ci.cancel();
        } else if (buffer instanceof SpriteCoordinateExpander consumer) {
            if (consumer.delegate instanceof ItemMeshEmitter emitter) {
                emitter.emit(root(), poseStack, consumer.sprite, null, lightCoords, overlayCoords, color);
                ci.cancel();
            }
        } else if (buffer instanceof DualVertexConsumer dual) {
            dual.emit(root(), poseStack, null, lightCoords, overlayCoords, color);
            ci.cancel();
        }
    }
}
