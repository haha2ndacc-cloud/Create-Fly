package com.zurrtum.create;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.Fluid;

import static com.zurrtum.create.Create.MOD_ID;

public class AllFluidIds {
    public static final ResourceKey<Fluid> FLOWING_POTION = create("flowing_potion");
    public static final ResourceKey<Fluid> POTION = create("potion");
    public static final ResourceKey<Fluid> FLOWING_TEA = create("flowing_tea");
    public static final ResourceKey<Fluid> TEA = create("tea");
    public static final ResourceKey<Fluid> FLOWING_MILK = create("flowing_milk");
    public static final ResourceKey<Fluid> MILK = create("milk");
    public static final ResourceKey<Fluid> FLOWING_HONEY = create("flowing_honey");
    public static final ResourceKey<Fluid> HONEY = create("honey");
    public static final ResourceKey<Fluid> FLOWING_CHOCOLATE = create("flowing_chocolate");
    public static final ResourceKey<Fluid> CHOCOLATE = create("chocolate");

    private static ResourceKey<Fluid> create(final String name) {
        return ResourceKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath(MOD_ID, name));
    }

    public static void init() {
    }
}
