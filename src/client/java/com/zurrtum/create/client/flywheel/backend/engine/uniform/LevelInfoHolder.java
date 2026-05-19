package com.zurrtum.create.client.flywheel.backend.engine.uniform;

import net.minecraft.client.multiplayer.ClientLevel;
import org.jspecify.annotations.Nullable;

public interface LevelInfoHolder {
    void flywheel$update(@Nullable ClientLevel level, float partialTick);

    int flywheel$ticks();

    int flywheel$levelDay();

    float flywheel$timeOfDay();

    int flywheel$skyLight();

    int flywheel$raining();

    int flywheel$thundering();

    float flywheel$thunderLevel();

    int flywheel$skyDarken();

    int flywheel$constantAmbientLight();

    int flywheel$dimensionId();
}
