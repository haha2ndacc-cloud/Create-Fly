package com.zurrtum.create;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import static com.zurrtum.create.Create.MOD_ID;

public class AllBlockTags {
    public static final TagKey<Block> CASING = AllBlockItemTags.CASING.block();
    public static final TagKey<Block> SEATS = AllBlockItemTags.SEATS.block();
    public static final TagKey<Block> POSTBOXES = AllBlockItemTags.POSTBOXES.block();
    public static final TagKey<Block> TABLE_CLOTHS = AllBlockItemTags.TABLE_CLOTHS.block();
    public static final TagKey<Block> TOOLBOXES = AllBlockItemTags.TOOLBOXES.block();
    public static final TagKey<Block> TRACKS = AllBlockItemTags.TRACKS.block();
    public static final TagKey<Block> VALVE_HANDLES = AllBlockItemTags.VALVE_HANDLES.block();
    public static final TagKey<Block> SAPLINGS = BlockItemTags.SAPLINGS.block();
    public static final TagKey<Block> BRITTLE = create("brittle");
    public static final TagKey<Block> COPYCAT_ALLOW = create("copycat_allow");
    public static final TagKey<Block> COPYCAT_DENY = create("copycat_deny");
    public static final TagKey<Block> FAN_PROCESSING_CATALYSTS_BLASTING = create("fan_processing_catalysts/blasting");
    public static final TagKey<Block> FAN_PROCESSING_CATALYSTS_HAUNTING = create("fan_processing_catalysts/haunting");
    public static final TagKey<Block> FAN_PROCESSING_CATALYSTS_SMOKING = create("fan_processing_catalysts/smoking");
    public static final TagKey<Block> FAN_PROCESSING_CATALYSTS_SPLASHING = create("fan_processing_catalysts/splashing");
    public static final TagKey<Block> FAN_TRANSPARENT = create("fan_transparent");
    public static final TagKey<Block> GIRDABLE_TRACKS = create("girdable_tracks");
    public static final TagKey<Block> MOVABLE_EMPTY_COLLIDER = create("movable_empty_collider");
    public static final TagKey<Block> NON_MOVABLE = create("non_movable");
    public static final TagKey<Block> NON_BREAKABLE = create("non_breakable");
    public static final TagKey<Block> PASSIVE_BOILER_HEATERS = create("passive_boiler_heaters");
    public static final TagKey<Block> SAFE_NBT = create("safe_nbt");
    public static final TagKey<Block> TREE_ATTACHMENTS = create("tree_attachments");
    public static final TagKey<Block> WINDMILL_SAILS = create("windmill_sails");
    public static final TagKey<Block> WRENCH_PICKUP = create("wrench_pickup");
    public static final TagKey<Block> CHEST_MOUNTED_STORAGE = create("chest_mounted_storage");
    public static final TagKey<Block> SIMPLE_MOUNTED_STORAGE = create("simple_mounted_storage");
    public static final TagKey<Block> FALLBACK_MOUNTED_STORAGE_BLACKLIST = create("fallback_mounted_storage_blacklist");
    public static final TagKey<Block> ROOTS = create("roots");
    public static final TagKey<Block> SUGAR_CANE_VARIANTS = create("sugar_cane_variants");
    public static final TagKey<Block> NON_HARVESTABLE = create("non_harvestable");
    public static final TagKey<Block> SINGLE_BLOCK_INVENTORIES = create("single_block_inventories");
    public static final TagKey<Block> PLOUGH_WHITELIST = create("plough_whitelist");
    public static final TagKey<Block> PLOUGH_BLACKLIST = create("plough_blacklist");

    public static final TagKey<Block> CORALS = create("corals");

    public static final TagKey<Block> RELOCATION_NOT_SUPPORTED = create("c", "relocation_not_supported");
    public static final TagKey<Block> CARDBOARD_STORAGE_BLOCKS = create("c", "storage_blocks/cardboard");
    public static final TagKey<Block> ANDESITE_ALLOY_STORAGE_BLOCKS = create("c", "storage_blocks/andesite_alloy");

    public static final TagKey<Block> SLIMY_LOGS = create("tconstruct", "slimy_logs");
    public static final TagKey<Block> NON_DOUBLE_DOOR = create("quark", "non_double_door");

    private static TagKey<Block> create(String name) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, name));
    }

    private static TagKey<Block> create(String namespace, String name) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(namespace, name));
    }

    public static void register() {
    }
}
