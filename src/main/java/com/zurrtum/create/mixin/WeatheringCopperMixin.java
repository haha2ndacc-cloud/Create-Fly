package com.zurrtum.create.mixin;

import com.google.common.collect.ImmutableBiMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.AllBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WeatheringCopper.class)
public interface WeatheringCopperMixin {
    @WrapOperation(method = "lambda$static$0()Lcom/google/common/collect/BiMap;", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableBiMap$Builder;build()Lcom/google/common/collect/ImmutableBiMap;", remap = false))
    private static ImmutableBiMap<Block, Block> addOxidizable(
        ImmutableBiMap.Builder<Block, Block> builder,
        Operation<ImmutableBiMap<Block, Block>> original
    ) {
        AllBlocks.COPPER_SHINGLES.weathering().progressMapping(builder::put);
        AllBlocks.COPPER_SHINGLE_SLAB.weathering().progressMapping(builder::put);
        AllBlocks.COPPER_SHINGLE_STAIRS.weathering().progressMapping(builder::put);
        AllBlocks.COPPER_TILES.weathering().progressMapping(builder::put);
        AllBlocks.COPPER_TILE_SLAB.weathering().progressMapping(builder::put);
        AllBlocks.COPPER_TILE_STAIRS.weathering().progressMapping(builder::put);
        return original.call(builder);
    }
}
