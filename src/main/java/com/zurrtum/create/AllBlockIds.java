package com.zurrtum.create;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

import static com.zurrtum.create.Create.MOD_ID;

public class AllBlockIds {
    public static final ResourceKey<Block> HONEY = create("honey");
    public static final ResourceKey<Block> CHOCOLATE = create("chocolate");

    private static ResourceKey<Block> create(final String name) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, name));
    }

    public static void init() {
    }
}
