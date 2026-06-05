package com.zurrtum.create.catnip.math;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class AngleHelper {

    public static float horizontalAngle(Direction facing) {
        return switch (facing) {
            case NORTH -> 180.0f;
            case WEST -> -90.0f;
            case EAST -> -270.0f;
            default -> 0.0f;
        };
    }

    public static float verticalAngle(Direction facing) {
        return switch (facing) {
            case UP -> -90.0f;
            case DOWN -> 90.0f;
            default -> 0.0f;
        };
    }

    public static float rad(double angle) {
        if (angle == 0) {
            return 0;
        }
        return (float) (angle / 180 * Math.PI);
    }

    public static float deg(double angle) {
        if (angle == 0) {
            return 0;
        }
        return (float) (angle * 180 / Math.PI);
    }

    public static float angleLerp(double pct, double current, double target) {
        return (float) (current + getShortestAngleDiff(current, target) * pct);
    }

    public static float getShortestAngleDiff(double current, double target) {
        current = current % 360;
        target = target % 360;
        return (float) (((target - current) % 360 + 540) % 360 - 180);
    }

    public static float getShortestAngleDiff(double current, double target, float hint) {
        float diff = getShortestAngleDiff(current, target);
        if (Mth.equal(Math.abs(diff), 180) && Math.signum(diff) != Math.signum(hint)) {
            return diff + 360 * Math.signum(hint);
        }
        return diff;
    }

    public static float wrapAngle180(float angle) {
        return (angle + 180) % 360 - 180;
    }

}
