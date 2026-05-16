package com.zurrtum.create;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static com.zurrtum.create.Create.MOD_ID;

public class AllItemTags {
    public static final TagKey<Item> CASING = AllBlockItemTags.CASING.item();
    public static final TagKey<Item> SEATS = AllBlockItemTags.SEATS.item();
    public static final TagKey<Item> POSTBOXES = AllBlockItemTags.POSTBOXES.item();
    public static final TagKey<Item> TABLE_CLOTHS = AllBlockItemTags.TABLE_CLOTHS.item();
    public static final TagKey<Item> TOOLBOXES = AllBlockItemTags.TOOLBOXES.item();
    public static final TagKey<Item> TRACKS = AllBlockItemTags.TRACKS.item();
    public static final TagKey<Item> VALVE_HANDLES = AllBlockItemTags.VALVE_HANDLES.item();
    public static final TagKey<Item> DOORS = BlockItemTags.DOORS.item();
    public static final TagKey<Item> BLAZE_BURNER_FUEL_REGULAR = create("blaze_burner_fuel/regular");
    public static final TagKey<Item> BLAZE_BURNER_FUEL_SPECIAL = create("blaze_burner_fuel/special");
    public static final TagKey<Item> CONTRAPTION_CONTROLLED = create("contraption_controlled");
    public static final TagKey<Item> CREATE_INGOTS = create("create_ingots");
    public static final TagKey<Item> CRUSHED_RAW_MATERIALS = create("crushed_raw_materials");
    public static final TagKey<Item> INVALID_FOR_TRACK_PAVING = create("invalid_for_track_paving");
    public static final TagKey<Item> DEPLOYABLE_DRINK = create("deployable_drink");
    public static final TagKey<Item> PRESSURIZED_AIR_SOURCES = create("pressurized_air_sources");
    public static final TagKey<Item> SANDPAPER = create("sandpaper");
    public static final TagKey<Item> DYED_TABLE_CLOTHS = create("dyed_table_cloths");
    public static final TagKey<Item> PULPIFIABLE = create("pulpifiable");
    public static final TagKey<Item> SLEEPERS = create("sleepers");
    public static final TagKey<Item> PACKAGES = create("packages");
    public static final TagKey<Item> CHAIN_RIDEABLE = create("chain_rideable");
    public static final TagKey<Item> UPRIGHT_ON_BELT = create("upright_on_belt");
    public static final TagKey<Item> NOT_UPRIGHT_ON_BELT = create("not_upright_on_belt");
    public static final TagKey<Item> NOT_POTION = create("not_potion");
    public static final TagKey<Item> DISPENSE_BEHAVIOR_WRAP_BLACKLIST = create("dispense_behavior_wrap_blacklist");
    public static final TagKey<Item> REPAIRS_COPPER_ARMOR = create("repairs_copper_armor");
    public static final TagKey<Item> REPAIRS_CARDBOARD_ARMOR = create("repairs_cardboard_armor");
    public static final TagKey<Item> ENCHANTMENT_KNOCKBACK = create("enchantment/knockback");
    public static final TagKey<Item> ENCHANTMENT_LOOTING = create("enchantment/looting");
    public static final TagKey<Item> ENCHANTMENT_DENY_MENDING = create("enchantment/deny_mending");
    public static final TagKey<Item> ENCHANTMENT_DENY_UNBREAKING = create("enchantment/deny_unbreaking");
    public static final TagKey<Item> ENCHANTMENT_DENY_INFINITY = create("enchantment/deny_infinity");
    public static final TagKey<Item> ENCHANTMENT_DENY_AQUA_AFFINITY = create("enchantment/deny_aqua_affinity");

    public static final TagKey<Item> PLATES = create("c", "plates");
    public static final TagKey<Item> OBSIDIAN_DUST = create("c", "dusts/obsidian");
    public static final TagKey<Item> DYES = create("c", "dyes");
    public static final TagKey<Item> SLIME_BALLS = create("c", "slime_balls");
    public static final TagKey<Item> TOOLS_WRENCH = create("c", "tools/wrench");
    public static final TagKey<Item> OBSIDIAN_PLATES = create("c", "plates/obsidian");
    public static final TagKey<Item> CARDBOARD_PLATES = create("c", "plates/cardboard");
    public static final TagKey<Item> CERTUS_QUARTZ = create("c", "gems/certus_quartz");
    public static final TagKey<Item> AMETRINE_ORES = create("c", "ores/ametrine");
    public static final TagKey<Item> ANTHRACITE_ORES = create("c", "ores/anthracite");
    public static final TagKey<Item> EMERALDITE_ORES = create("c", "ores/emeraldite");
    public static final TagKey<Item> LIGNITE_ORES = create("c", "ores/lignite");
    public static final TagKey<Item> CARDBOARD_STORAGE_BLOCKS = create("c", "storage_blocks/cardboard");
    public static final TagKey<Item> ANDESITE_ALLOY_STORAGE_BLOCKS = create("c", "storage_blocks/andesite_alloy");
    public static final TagKey<Item> CHOCOLATE_BUCKETS = create("c", "buckets/chocolate");
    public static final TagKey<Item> HONEY_BUCKETS = create("c", "buckets/honey");
    public static final TagKey<Item> FOODS_CHOCOLATE = create("c", "foods/chocolate");
    public static final TagKey<Item> DRINKS_TEA = create("c", "drinks/tea");
    public static final TagKey<Item> FLOURS = create("c", "flours");
    public static final TagKey<Item> WHEAT_FLOURS = create("c", "flours/wheat");
    public static final TagKey<Item> WHEAT_DOUGHS = create("c", "foods/dough/wheat");

    public static final TagKey<Item> ALLURITE = create("stone_types/galosphere/allurite");
    public static final TagKey<Item> AMETHYST = create("stone_types/galosphere/amethyst");
    public static final TagKey<Item> LUMIERE = create("stone_types/galosphere/lumiere");

    public static final TagKey<Item> UA_CORAL = create("upgrade_aquatic/coral");
    public static final TagKey<Item> CURIOS_HEAD = create("curios", "head");

    private static TagKey<Item> create(String name) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
    }

    private static TagKey<Item> create(String namespace, String name) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(namespace, name));
    }

    private static final Map<TagKey<Item>, DyeColor> dyesTag = Util.make(
        new HashMap<>(), map -> {
            for (DyeColor color : DyeColor.values()) {
                map.put(
                    TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dyes/" + color.getName())),
                    color
                );
            }
        }
    );

    @Nullable
    public static DyeColor getDyeColor(ItemStack stack) {
        DyeColor color = stack.get(DataComponents.DYE);
        if (color != null) {
            return color;
        }
        return dyesTag.entrySet().stream().filter(entry -> stack.is(entry.getKey())).map(Map.Entry::getValue).findAny()
            .orElse(null);
    }

    public static void register() {
    }
}
