package com.zurrtum.create.client.mixin;

import cc.cassian.rrv.common.rendering.RrvGuiRenderHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(RrvGuiRenderHelper.class)
public class RrvGuiRenderHelperMixin {
    @Unique
    private static final AtomicInteger ENTITY_COUNTER = new AtomicInteger();

    @Inject(method = "renderEntityOnScreen(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/LivingEntity;IIIIFLorg/joml/Vector3f;Lorg/joml/Quaternionf;Lorg/joml/Quaternionf;)V", at = @At("HEAD"))
    private static void checkEntity(
        GuiGraphicsExtractor guiGraphics,
        LivingEntity livingEntity,
        int x0,
        int y0,
        int x1,
        int y1,
        float scale,
        Vector3f translation,
        Quaternionf rotation,
        Quaternionf cameraAngleOverride,
        CallbackInfo ci
    ) {
        try {
            livingEntity.getId();
        } catch (Exception ignore) {
            livingEntity.setId(ENTITY_COUNTER.incrementAndGet());
        }
    }
}
