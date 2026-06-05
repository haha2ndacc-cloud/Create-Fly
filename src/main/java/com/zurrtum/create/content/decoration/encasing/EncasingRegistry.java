package com.zurrtum.create.content.decoration.encasing;

import com.zurrtum.create.AllBlocks;
import net.minecraft.world.level.block.Block;

import java.util.*;

public class EncasingRegistry {
    private static final Map<Block, List<Block>> ENCASED_VARIANTS = new HashMap<>();

    /**
     * <strong>This method must not be called before block registration is finished.</strong>
     */
    public static <B extends Block & EncasableBlock, E extends Block & EncasedBlock, P> void addVariant(
        B encasable,
        E encased
    ) {
        ENCASED_VARIANTS.computeIfAbsent(encasable, b -> new ArrayList<>()).add(encased);
    }

    public static List<Block> getVariants(Block block) {
        return ENCASED_VARIANTS.getOrDefault(block, Collections.emptyList());
    }

    public static void register() {
        addVariant(AllBlocks.SHAFT, AllBlocks.ANDESITE_ENCASED_SHAFT);
        addVariant(AllBlocks.SHAFT, AllBlocks.BRASS_ENCASED_SHAFT);
        addVariant(AllBlocks.COGWHEEL, AllBlocks.ANDESITE_ENCASED_COGWHEEL);
        addVariant(AllBlocks.COGWHEEL, AllBlocks.BRASS_ENCASED_COGWHEEL);
        addVariant(AllBlocks.LARGE_COGWHEEL, AllBlocks.ANDESITE_ENCASED_LARGE_COGWHEEL);
        addVariant(AllBlocks.LARGE_COGWHEEL, AllBlocks.BRASS_ENCASED_LARGE_COGWHEEL);
        addVariant(AllBlocks.FLUID_PIPE, AllBlocks.ENCASED_FLUID_PIPE);
    }
}
