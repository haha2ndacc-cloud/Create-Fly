package com.zurrtum.create;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import static com.zurrtum.create.Create.MOD_ID;

public class AllFluidTags {
    public static final TagKey<Fluid> BOTTOMLESS_ALLOW = create("bottomless/allow");
    public static final TagKey<Fluid> BOTTOMLESS_DENY = create("bottomless/deny");
    public static final TagKey<Fluid> FAN_PROCESSING_CATALYSTS_BLASTING = create("fan_processing_catalysts/blasting");
    public static final TagKey<Fluid> FAN_PROCESSING_CATALYSTS_HAUNTING = create("fan_processing_catalysts/haunting");
    public static final TagKey<Fluid> FAN_PROCESSING_CATALYSTS_SMOKING = create("fan_processing_catalysts/smoking");
    public static final TagKey<Fluid> FAN_PROCESSING_CATALYSTS_SPLASHING = create("fan_processing_catalysts/splashing");
    public static final TagKey<Fluid> MILK = createCommon("milk");
    public static final TagKey<Fluid> TEA = createCommon("tea");
    public static final TagKey<Fluid> CHOCOLATE = createCommon("chocolate");
    public static final TagKey<Fluid> CREOSOTE = createCommon("creosote");

    private static TagKey<Fluid> create(String name) {
        return TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath(MOD_ID, name));
    }

    private static TagKey<Fluid> createCommon(String name) {
        return TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath("c", name));
    }

    public static void register() {
    }
}
