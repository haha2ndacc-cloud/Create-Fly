package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.placement.PlacementClient;
import com.zurrtum.create.client.content.equipment.armor.CardboardArmorStealthOverlay;
import com.zurrtum.create.client.content.equipment.armor.RemainingAirOverlay;
import com.zurrtum.create.client.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.zurrtum.create.client.content.equipment.goggles.GoggleOverlayRenderer;
import com.zurrtum.create.client.content.equipment.toolbox.ToolboxHandlerClient;
import com.zurrtum.create.client.content.redstone.link.controller.LinkedControllerClientHandler;
import com.zurrtum.create.client.content.trains.TrainHUD;
import com.zurrtum.create.client.content.trains.track.TrackPlacementOverlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.Hud.ContextualInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class HudMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @WrapOperation(method = "extractHotbarAndDecorations(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;nextContextualInfoState()Lnet/minecraft/client/gui/Hud$ContextualInfo;"))
    private ContextualInfo renderMainHud(
        Hud instance,
        Operation<ContextualInfo> original,
        @Local(argsOnly = true) GuiGraphicsExtractor graphics,
        @Local(argsOnly = true) DeltaTracker deltaTracker
    ) {
        if (TrainHUD.renderOverlay(minecraft, graphics, deltaTracker)) {
            return ContextualInfo.EMPTY;
        }
        return original.call(instance);
    }

    @Inject(method = "extractCrosshair(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At("TAIL"))
    private void extractCrosshair(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        PlacementClient.onRenderCrosshairOverlay(
            minecraft,
            graphics,
            AnimationTickHolder.getPartialTicksUI(deltaTracker)
        );
    }

    @Inject(method = "extractItemHotbar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At("TAIL"))
    private void extractItemHotbar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Create.VALUE_SETTINGS_HANDLER.render(minecraft, graphics);
        TrackPlacementOverlay.render(minecraft, graphics);
        GoggleOverlayRenderer.renderOverlay(minecraft, graphics, deltaTracker);
        BlueprintOverlayRenderer.renderOverlay(minecraft, graphics);
        LinkedControllerClientHandler.renderOverlay(minecraft, graphics);
        Create.SCHEMATIC_HANDLER.render(minecraft, graphics, deltaTracker);
        ToolboxHandlerClient.renderOverlay(minecraft, graphics);
    }

    @Inject(method = "extractAirBubbles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;III)V", at = @At("TAIL"))
    private void extractAirBubbles(
        GuiGraphicsExtractor graphics,
        Player player,
        int vehicleHearts,
        int yLineAir,
        int xRight,
        CallbackInfo ci
    ) {
        RemainingAirOverlay.render(minecraft, graphics);
    }

    @WrapOperation(method = "extractCameraOverlays(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractTextureOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/resources/Identifier;F)V", ordinal = 0))
    private void renderMiscOverlays(
        Hud instance,
        GuiGraphicsExtractor graphics,
        Identifier texture,
        float alpha,
        Operation<Void> original,
        @Local(argsOnly = true) DeltaTracker deltaTracker,
        @Local ItemStack item
    ) {
        if (item.is(AllItems.CARDBOARD_HELMET)) {
            original.call(instance, graphics, texture, CardboardArmorStealthOverlay.getOverlayOpacity(deltaTracker));
        } else {
            original.call(instance, graphics, texture, alpha);
        }
    }
}
