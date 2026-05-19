package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.content.equipment.armor.NetheriteBacktankFirstPersonRenderer;
import com.zurrtum.create.client.content.equipment.extendoGrip.ExtendoGripRenderHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private EntityRenderDispatcher entityRenderDispatcher;

    @WrapOperation(method = "submitHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;submitArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"))
    private void renderItem(
        ItemInHandRenderer instance,
        AbstractClientPlayer player,
        float frameInterp,
        float xRot,
        InteractionHand hand,
        float attack,
        ItemStack itemStack,
        float inverseArmHeight,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        int lightCoords,
        Operation<Void> original
    ) {
        if (Create.ZAPPER_RENDER_HANDLER.onRenderPlayerHand(
            itemStack,
            minecraft,
            entityRenderDispatcher,
            instance,
            poseStack,
            submitNodeCollector,
            lightCoords,
            frameInterp,
            hand,
            inverseArmHeight,
            attack
        ) || Create.POTATO_CANNON_RENDER_HANDLER.onRenderPlayerHand(
            itemStack,
            minecraft,
            entityRenderDispatcher,
            instance,
            poseStack,
            submitNodeCollector,
            lightCoords,
            frameInterp,
            hand,
            inverseArmHeight,
            attack
        ) || ExtendoGripRenderHandler.onRenderPlayerHand(
            itemStack,
            minecraft,
            entityRenderDispatcher,
            poseStack,
            submitNodeCollector,
            lightCoords,
            hand,
            inverseArmHeight,
            attack
        )) {
            return;
        }
        original.call(
            instance,
            player,
            frameInterp,
            xRot,
            hand,
            attack,
            itemStack,
            inverseArmHeight,
            poseStack,
            submitNodeCollector,
            lightCoords
        );
    }

    @WrapOperation(method = "renderMapHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/world/entity/HumanoidArm;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/ClientAsset$Texture;texturePath()Lnet/minecraft/resources/Identifier;"))
    private Identifier getMapHandTexture(ClientAsset.Texture instance, Operation<Identifier> original) {
        Identifier id = NetheriteBacktankFirstPersonRenderer.getHandTexture(minecraft.player);
        if (id != null) {
            return id;
        }
        return original.call(instance);
    }

    @WrapOperation(method = "renderPlayerArm(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IFFLnet/minecraft/world/entity/HumanoidArm;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/ClientAsset$Texture;texturePath()Lnet/minecraft/resources/Identifier;"))
    private Identifier getHandTexture(ClientAsset.Texture instance, Operation<Identifier> original) {
        Identifier id = NetheriteBacktankFirstPersonRenderer.getHandTexture(minecraft.player);
        if (id != null) {
            return id;
        }
        return original.call(instance);
    }
}
