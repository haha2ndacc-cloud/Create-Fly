package com.zurrtum.create.client.content.processing.burner;

import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.flywheel.api.instance.InstanceHandle;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.lib.instance.ColoredLitOverlayInstance;

public class ScrollStepInstance extends ScrollInstance {
    public float stepU = 1;
    public float stepV = 1;

    public ScrollStepInstance(InstanceType<? extends ColoredLitOverlayInstance> type, InstanceHandle handle) {
        super(type, handle);
    }

    public ScrollStepInstance setSpriteShift(
        SpriteShiftEntry spriteShift,
        float factorU,
        float factorV,
        float factorStepU,
        float factorStepV
    ) {
        float spriteWidth = spriteShift.getTarget().getU1() - spriteShift.getTarget().getU0();
        float spriteHeight = spriteShift.getTarget().getV1() - spriteShift.getTarget().getV0();

        scaleU = spriteWidth * factorU * factorStepU;
        scaleV = spriteHeight * factorV * factorStepV;
        stepU = factorStepU;
        stepV = factorStepV;

        diffU = spriteShift.getTarget().getU0() - spriteShift.getOriginal().getU0();
        diffV = spriteShift.getTarget().getV0() - spriteShift.getOriginal().getV0();

        return this;
    }
}
