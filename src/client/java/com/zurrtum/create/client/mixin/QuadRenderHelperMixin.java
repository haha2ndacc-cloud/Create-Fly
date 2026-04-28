package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.catnip.render.QuadRenderHelper;
import net.caffeinemc.mods.sodium.api.texture.SpriteUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
@Mixin(QuadRenderHelper.class)
public class QuadRenderHelperMixin {
    @Unique
    private static final SpriteUtil SODIUM = SpriteUtil.INSTANCE;

    @Overwrite(remap = false)
    public static void markSpriteActive(TextureAtlasSprite sprite) {
        SODIUM.markSpriteActive(sprite);
    }

    @Overwrite(remap = false)
    public static void markFluidSpriteActive(BlockGetter getter, Iterable<BlockPos> section) {
        FluidStateModelSet fluidStateModelSet = Minecraft.getInstance().getModelManager().getFluidStateModelSet();
        Set<Fluid> fluids = new HashSet<>();
        for (BlockPos pos : section) {
            BlockState state = getter.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }
            FluidState fluidState = state.getFluidState();
            if (fluidState.isEmpty()) {
                continue;
            }
            if (fluids.add(fluidState.getType())) {
                FluidModel model = fluidStateModelSet.get(fluidState);
                SODIUM.markSpriteActive(model.stillMaterial().sprite());
                SODIUM.markSpriteActive(model.flowingMaterial().sprite());
            }
        }
    }
}
