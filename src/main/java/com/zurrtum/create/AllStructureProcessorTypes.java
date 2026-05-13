package com.zurrtum.create;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.content.schematics.SchematicProcessor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

import static com.zurrtum.create.Create.MOD_ID;

public class AllStructureProcessorTypes {
    public static final Identifier SCHEMATIC = register("schematic", SchematicProcessor.MAP_CODEC);

    public static <P extends StructureProcessor> Identifier register(String name, MapCodec<P> codec) {
        Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, name);
        Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, id, codec);
        return id;
    }

    public static void register() {
    }
}
