package com.zurrtum.create.client.foundation.blockEntity.behaviour.audio;

import com.zurrtum.create.client.foundation.sound.SoundScapes;
import com.zurrtum.create.client.foundation.sound.SoundScapes.AmbienceGroup;
import com.zurrtum.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import net.minecraft.util.Mth;

public class CrushingWheelControllerAudioBehaviour extends AudioBehaviour<CrushingWheelControllerBlockEntity> {
    public CrushingWheelControllerAudioBehaviour(CrushingWheelControllerBlockEntity be) {
        super(be);
    }

    @Override
    public void tickAudio() {
        if (!blockEntity.isOccupied() || blockEntity.crushingspeed == 0) {
            return;
        }
        float pitch = Mth.clamp(blockEntity.crushingspeed / 256.0f + 0.45f, 0.85f, 1.0f);
        if (blockEntity.entityUUID == null && blockEntity.inventory.getItem(0).isEmpty()) {
            return;
        }
        SoundScapes.play(AmbienceGroup.CRUSHING, blockEntity.getBlockPos(), pitch);
    }
}
