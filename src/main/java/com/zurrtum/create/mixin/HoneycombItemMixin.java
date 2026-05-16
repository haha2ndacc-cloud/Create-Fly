package com.zurrtum.create.mixin;

import com.google.common.collect.ImmutableBiMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.AllBlocks;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HoneycombItem.class)
public class HoneycombItemMixin {
    @WrapOperation(method = "lambda$static$0()Lcom/google/common/collect/BiMap;", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableBiMap$Builder;build()Lcom/google/common/collect/ImmutableBiMap;", remap = false))
    private static ImmutableBiMap<Block, Block> addWaxed(
        ImmutableBiMap.Builder<Block, Block> builder,
        Operation<ImmutableBiMap<Block, Block>> original
    ) {
        AllBlocks.COPPER_SHINGLES.zipUnwaxedWaxed(builder::put);
        AllBlocks.COPPER_SHINGLE_SLAB.zipUnwaxedWaxed(builder::put);
        AllBlocks.COPPER_SHINGLE_STAIRS.zipUnwaxedWaxed(builder::put);
        AllBlocks.COPPER_TILES.zipUnwaxedWaxed(builder::put);
        AllBlocks.COPPER_TILE_SLAB.zipUnwaxedWaxed(builder::put);
        AllBlocks.COPPER_TILE_STAIRS.zipUnwaxedWaxed(builder::put);
        return original.call(builder);
    }
}
