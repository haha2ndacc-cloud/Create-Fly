package com.zurrtum.create.client.foundation.utility;

import com.mojang.serialization.Codec;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.math.AngleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public class CameraAngleAnimationService {
    private static final LerpedFloat yRotation = LerpedFloat.angular().startWithValue(0);
    private static final LerpedFloat xRotation = LerpedFloat.angular().startWithValue(0);

    private static Mode animationMode = Mode.LINEAR;
    private static float animationSpeed = -1;

    public static void tick(Minecraft mc) {
        yRotation.tickChaser();
        xRotation.tickChaser();
        LocalPlayer player = mc.player;
        if (player != null) {
            if (!yRotation.settled()) {
                player.setYRot(yRotation.getValue(1));
            }
            if (!xRotation.settled()) {
                player.setXRot(xRotation.getValue(1));
            }
        }
    }

    public static boolean isYawAnimating() {
        return !yRotation.settled();
    }

    public static boolean isPitchAnimating() {
        return !xRotation.settled();
    }

    public static float getYaw(float partialTicks) {
        return yRotation.getValue(partialTicks);
    }

    public static float getPitch(float partialTicks) {
        return xRotation.getValue(partialTicks);
    }

    public static void setAnimationMode(Mode mode) {
        animationMode = mode;
    }

    public static void setAnimationSpeed(float speed) {
        animationSpeed = speed;
    }

    public static void setYawTarget(float yaw) {
        float currentYaw = getCurrentYaw();
        yRotation.startWithValue(currentYaw);
        setupChaser(yRotation, currentYaw + AngleHelper.getShortestAngleDiff(currentYaw, Mth.wrapDegrees(yaw)));
    }

    public static void setPitchTarget(float pitch) {
        float currentPitch = getCurrentPitch();
        xRotation.startWithValue(currentPitch);
        setupChaser(xRotation, currentPitch + AngleHelper.getShortestAngleDiff(currentPitch, Mth.wrapDegrees(pitch)));
    }

    private static float getCurrentYaw() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return 0;
        }
        return Mth.wrapDegrees(player.getYRot());
    }

    private static float getCurrentPitch() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return 0;
        }
        return Mth.wrapDegrees(player.getXRot());
    }

    private static void setupChaser(LerpedFloat rotation, float target) {
        if (animationMode == Mode.LINEAR) {
            rotation.chase(target, animationSpeed > 0 ? animationSpeed : 2, LerpedFloat.Chaser.LINEAR);
        } else if (animationMode == Mode.EXPONENTIAL) {
            rotation.chase(target, animationSpeed > 0 ? animationSpeed : 0.25, LerpedFloat.Chaser.EXP);
        }
    }

    public static void register() {
        ArgumentTypeInfos.BY_CLASS.put(ModeArgument.class, ModeArgument.INFO);
    }

    public enum Mode implements StringRepresentable {
        LINEAR, EXPONENTIAL;
        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static class ModeArgument extends StringRepresentableArgument<Mode> {
        public static final ModeArgument INSTANCE = new ModeArgument();
        public static final SingletonArgumentInfo<ModeArgument> INFO = SingletonArgumentInfo.contextFree(() -> INSTANCE);

        public ModeArgument() {
            super(Mode.CODEC, Mode::values);
        }
    }
}