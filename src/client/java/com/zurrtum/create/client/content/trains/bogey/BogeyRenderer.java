package com.zurrtum.create.client.content.trains.bogey;

import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityRenderer.BogeyRenderState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.CardinalLighting;
import org.jspecify.annotations.Nullable;

public interface BogeyRenderer {
    BogeyRenderState getRenderData(
        CompoundTag bogeyData,
        float wheelAngle,
        float tickProgress,
        int light,
        @Nullable CardinalLighting cardinalLighting,
        boolean inContraption
    );
}
