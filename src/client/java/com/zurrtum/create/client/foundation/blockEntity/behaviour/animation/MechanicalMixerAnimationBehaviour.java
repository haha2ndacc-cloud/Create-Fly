package com.zurrtum.create.client.foundation.blockEntity.behaviour.animation;

import com.zurrtum.create.content.kinetics.mixer.MechanicalMixerBlockEntity;

public class MechanicalMixerAnimationBehaviour extends AnimationBehaviour<MechanicalMixerBlockEntity> {
    private float offset;

    public MechanicalMixerAnimationBehaviour(MechanicalMixerBlockEntity be) {
        super(be);
    }

    public float getOffset(float speed, float tickProgress) {
        if (blockEntity.running) {
            int runningTicks = blockEntity.runningTicks;
            if (runningTicks < 15) {
                return offset + speed * 0.3f * tickProgress;
            }
            if (runningTicks <= 20) {
                return offset + speed * 0.9f * tickProgress;
            }
            return offset + speed * 0.3f * tickProgress;
        }
        return offset;
    }

    @Override
    public void tickAnimation() {
        if (blockEntity.running) {
            int runningTicks = blockEntity.runningTicks;
            float speed = blockEntity.getSpeed();
            if (runningTicks < 15) {
                offset = (offset + speed * 0.3f) % 360;
            } else if (runningTicks <= 20) {
                offset = (offset + speed * 0.9f) % 360;
            } else {
                offset = (offset + speed * 0.3f) % 360;
            }
        }
    }
}
