package com.zurrtum.create;

import com.mojang.serialization.Codec;
import com.zurrtum.create.content.equipment.clipboard.ClipboardTypePredicate;
import net.minecraft.core.Registry;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.component.predicates.DataComponentPredicate.Type;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import static com.zurrtum.create.Create.MOD_ID;

public class AllDataComponentPredicates {
    public static final Type<ClipboardTypePredicate> CLIPBOARD_TYPE = register(
        "clipboard_type",
        ClipboardTypePredicate.CODEC
    );

    private static <T extends DataComponentPredicate> DataComponentPredicate.Type<T> register(
        final String id,
        final Codec<T> codec
    ) {
        return Registry.register(
            BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE,
            Identifier.fromNamespaceAndPath(MOD_ID, id),
            new DataComponentPredicate.ConcreteType<>(codec)
        );
    }

    public static void register() {
    }
}
