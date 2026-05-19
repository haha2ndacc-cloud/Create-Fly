package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.flywheel.lib.visualization.VisualizationHelper;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SectionCompiler.class)
public class SectionCompilerMixin {
    @Inject(method = "handleBlockEntity", at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity> void flywheel$tryAddBlockEntity(
        SectionCompiler.Results results,
        E blockEntity,
        CallbackInfo ci
    ) {
        if (VisualizationHelper.tryAddBlockEntity(blockEntity)) {
            ci.cancel();
        }
    }
}
