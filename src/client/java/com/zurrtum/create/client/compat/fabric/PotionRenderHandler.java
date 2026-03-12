package com.zurrtum.create.client.compat.fabric;

import com.zurrtum.create.AllFluids;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.infrastructure.fluid.FluidTintSource;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;

public record PotionRenderHandler(FluidTintSource source) implements FluidVariantRenderHandler {
    public static final PotionRenderHandler INSTANCE = new PotionRenderHandler(AllFluidConfigs.TINT.get(AllFluids.POTION));

    @Override
    public int getColor(FluidVariant fluidVariant, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos) {
        return source.get(fluidVariant.getFluid(), fluidVariant.getComponentsPatch());
    }

    public static void register() {
        FluidVariantRendering.register(AllFluids.POTION, INSTANCE);
    }
}
