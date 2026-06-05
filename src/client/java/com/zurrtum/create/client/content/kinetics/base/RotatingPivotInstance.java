package com.zurrtum.create.client.content.kinetics.base;

import com.zurrtum.create.client.flywheel.api.instance.InstanceHandle;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

public class RotatingPivotInstance extends RotatingInstance {
    public float pivotX;
    public float pivotY;
    public float pivotZ;

    public RotatingPivotInstance(InstanceType<? extends RotatingInstance> type, InstanceHandle handle) {
        super(type, handle);
    }

    public RotatingPivotInstance pivot(float x, float y, float z) {
        pivotX = x;
        pivotY = y;
        pivotZ = z;
        return this;
    }

    public RotatingPivotInstance pivot(Vector3fc pos) {
        return pivot(pos.x(), pos.y(), pos.z());
    }

    public RotatingPivotInstance pivot(Vec3i pos) {
        return pivot(pos.getX(), pos.getY(), pos.getZ());
    }

    public RotatingPivotInstance pivot(Vec3 pos) {
        return pivot((float) pos.x(), (float) pos.y(), (float) pos.z());
    }

    public RotatingPivotInstance translatePivot(float x, float y, float z) {
        pivotX += x;
        pivotY += y;
        pivotZ += z;
        return this;
    }
}
