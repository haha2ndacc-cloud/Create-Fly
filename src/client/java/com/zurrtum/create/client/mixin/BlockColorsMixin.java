package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.infrastructure.model.CopycatModel.WrappedBlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(BlockColors.class)
public class BlockColorsMixin {
    @ModifyReturnValue(method = "createDefault()Lnet/minecraft/client/color/block/BlockColors;", at = @At("TAIL"))
    private static BlockColors addColors(BlockColors blockColors) {
        blockColors.register(List.of(BlockTintSources.redstone()), AllBlocks.CONTROLLER_RAIL);
        blockColors.register(
            List.of(
                new WrappedBlockColor(blockColors, 0),
                new WrappedBlockColor(blockColors, 1),
                new WrappedBlockColor(blockColors, 2)
            ), AllBlocks.COPYCAT_STEP, AllBlocks.COPYCAT_PANEL
        );
        return blockColors;
    }
}
