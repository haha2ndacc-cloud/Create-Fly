package com.zurrtum.create.client;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class AllBlockTints {
    public record WrappedBlockColor(BlockColors blockColors, int tintIndex) implements BlockTintSource {
        @Override
        public int color(BlockState state) {
            return GrassColor.getDefaultColor();
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            List<BlockTintSource> tintSources = blockColors.getTintSources(CopycatBlock.getMaterial(level, pos));
            if (tintSources.size() <= tintIndex) {
                return -1;
            }
            return tintSources.get(tintIndex).colorInWorld(state, level, pos);
        }

        @Override
        public int colorAsTerrainParticle(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            List<BlockTintSource> tintSources = blockColors.getTintSources(CopycatBlock.getMaterial(level, pos));
            if (tintSources.size() <= tintIndex) {
                return -1;
            }
            return tintSources.get(tintIndex).colorAsTerrainParticle(state, level, pos);
        }
    }

    public static void register(BlockColors blockColors) {
        blockColors.register(List.of(BlockTintSources.redstone()), AllBlocks.CONTROLLER_RAIL);
        blockColors.register(
            List.of(
                new WrappedBlockColor(blockColors, 0),
                new WrappedBlockColor(blockColors, 1),
                new WrappedBlockColor(blockColors, 2)
            ), AllBlocks.COPYCAT_STEP, AllBlocks.COPYCAT_PANEL
        );
    }
}
