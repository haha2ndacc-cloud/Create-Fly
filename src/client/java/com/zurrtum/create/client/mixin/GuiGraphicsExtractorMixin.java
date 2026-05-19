package com.zurrtum.create.client.mixin;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.infrastructure.model.PotatoCannonModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsExtractorMixin {
    @Shadow
    @Final
    public Minecraft minecraft;

    @Inject(method = "itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;popMatrix()Lorg/joml/Matrix3x2fStack;", remap = false))
    private void drawStackOverlay(Font font, ItemStack itemStack, int x, int y, String countText, CallbackInfo ci) {
        if (itemStack.is(AllItems.POTATO_CANNON)) {
            PotatoCannonModel.renderDecorator(minecraft, (GuiGraphicsExtractor) (Object) this, itemStack, x, y);
        }
    }
}
