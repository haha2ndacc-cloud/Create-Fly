package com.zurrtum.create.foundation.block;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public interface EntityControlBlock {
    void onEntityMovement(Level level, Entity entity);
}
