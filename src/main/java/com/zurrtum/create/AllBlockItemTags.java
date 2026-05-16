package com.zurrtum.create;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockItemTagId;

import static com.zurrtum.create.Create.MOD_ID;

public class AllBlockItemTags {
    public static final BlockItemTagId CASING = create("casing");
    public static final BlockItemTagId SEATS = create("seats");
    public static final BlockItemTagId POSTBOXES = create("postboxes");
    public static final BlockItemTagId TABLE_CLOTHS = create("table_cloths");
    public static final BlockItemTagId TOOLBOXES = create("toolboxes");
    public static final BlockItemTagId TRACKS = create("tracks");
    public static final BlockItemTagId VALVE_HANDLES = create("valve_handles");

    private static BlockItemTagId create(final String name) {
        Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, name);
        return BlockItemTagId.create(id, id);
    }

    public static void register() {
    }
}
