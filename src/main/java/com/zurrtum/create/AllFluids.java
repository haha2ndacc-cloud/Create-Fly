package com.zurrtum.create;

import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import com.zurrtum.create.infrastructure.fluids.FluidBlock;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import static net.minecraft.world.level.material.Fluids.register;

public class AllFluids {
    public static final FlowableFluid FLOWING_POTION = register(
        AllFluidIds.FLOWING_POTION,
        AllFluidEntries.POTION.flowing
    );
    public static final FlowableFluid POTION = register(AllFluidIds.POTION, AllFluidEntries.POTION.still);
    public static final FlowableFluid FLOWING_TEA = register(AllFluidIds.FLOWING_TEA, AllFluidEntries.TEA.flowing);
    public static final FlowableFluid TEA = register(AllFluidIds.TEA, AllFluidEntries.TEA.still);
    public static final FlowableFluid FLOWING_MILK = register(AllFluidIds.FLOWING_MILK, AllFluidEntries.MILK.flowing);
    public static final FlowableFluid MILK = register(AllFluidIds.MILK, AllFluidEntries.MILK.still);
    public static final FlowableFluid FLOWING_HONEY = register(
        AllFluidIds.FLOWING_HONEY,
        AllFluidEntries.HONEY.flowing
    );
    public static final FlowableFluid HONEY = register(AllFluidIds.HONEY, AllFluidEntries.HONEY.still);
    public static final FlowableFluid FLOWING_CHOCOLATE = register(
        AllFluidIds.FLOWING_CHOCOLATE,
        AllFluidEntries.CHOCOLATE.flowing
    );
    public static final FlowableFluid CHOCOLATE = register(AllFluidIds.CHOCOLATE, AllFluidEntries.CHOCOLATE.still);

    protected static Block honeyBlock(BlockBehaviour.Properties properties) {
        return new FluidBlock(HONEY, properties);
    }

    protected static Item honeyBucket(Item.Properties properties) {
        return new BucketItem(HONEY, properties);
    }

    protected static Block chocolateBlock(BlockBehaviour.Properties properties) {
        return new FluidBlock(CHOCOLATE, properties);
    }

    protected static Item chocolateBucket(Item.Properties properties) {
        return new BucketItem(CHOCOLATE, properties);
    }

    public static void init() {
    }
}
