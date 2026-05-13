package com.zurrtum.create.foundation.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.PlayerAdvancements.TriggerInstanceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.ValidationContextSource;

import java.util.Map;

public record CreateTrigger(Identifier id) implements CriterionTrigger<CreateTrigger.Conditions> {
    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer player) {
        PlayerAdvancements advancements = player.getAdvancements();
        Map<TriggerInstanceKey, Conditions> listenersForType = advancements.getTriggerMapForType(this);
        if (listenersForType != null) {
            for (Map.Entry<TriggerInstanceKey, Conditions> entry : listenersForType.entrySet()) {
                TriggerInstanceKey criterion = entry.getKey();
                advancements.award(criterion.advancement(), criterion.criterion());
            }
        }
    }

    public static class Conditions implements CriterionTriggerInstance {
        public static final Codec<Conditions> CODEC = MapCodec.unitCodec(new Conditions());

        @Override
        public void validate(ValidationContextSource validator) {
        }
    }
}