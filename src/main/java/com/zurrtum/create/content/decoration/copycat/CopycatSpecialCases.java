package com.zurrtum.create.content.decoration.copycat;

import com.zurrtum.create.content.decoration.palettes.GlassPaneBlock;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

public class CopycatSpecialCases {

    public static boolean isBarsMaterial(BlockState material) {
        Block block = material.getBlock();
        return block instanceof IronBarsBlock && !(block instanceof GlassPaneBlock) && !(block instanceof StainedGlassPaneBlock) && block != Blocks.GLASS_PANE;
    }

    public static boolean isTrapdoorMaterial(BlockState material) {
        return material.getBlock() instanceof TrapDoorBlock && material.hasProperty(TrapDoorBlock.HALF) && material.hasProperty(
            TrapDoorBlock.OPEN) && material.hasProperty(TrapDoorBlock.FACING);
    }

}
