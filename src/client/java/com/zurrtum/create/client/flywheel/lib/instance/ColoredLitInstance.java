package com.zurrtum.create.client.flywheel.lib.instance;

import com.zurrtum.create.client.flywheel.api.instance.InstanceHandle;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import net.minecraft.util.ARGB;

public abstract class ColoredLitInstance extends AbstractInstance implements FlatLit {
    public byte red = (byte) 0xFF;
    public byte green = (byte) 0xFF;
    public byte blue = (byte) 0xFF;
    public byte alpha = (byte) 0xFF;

    public int light;

    public ColoredLitInstance(InstanceType<? extends ColoredLitInstance> type, InstanceHandle handle) {
        super(type, handle);
    }

    public ColoredLitInstance colorArgb(int argb) {
        return color(ARGB.red(argb), ARGB.green(argb), ARGB.blue(argb), ARGB.alpha(argb));
    }

    public ColoredLitInstance colorRgb(int rgb) {
        return color(ARGB.red(rgb), ARGB.green(rgb), ARGB.blue(rgb));
    }

    public ColoredLitInstance color(int red, int green, int blue, int alpha) {
        return color((byte) red, (byte) green, (byte) blue, (byte) alpha);
    }

    public ColoredLitInstance color(int red, int green, int blue) {
        return color((byte) red, (byte) green, (byte) blue);
    }

    public ColoredLitInstance color(byte red, byte green, byte blue, byte alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        return this;
    }

    public ColoredLitInstance color(byte red, byte green, byte blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        return this;
    }

    public ColoredLitInstance color(float red, float green, float blue, float alpha) {
        return color((byte) (red * 255.0f), (byte) (green * 255.0f), (byte) (blue * 255.0f), (byte) (alpha * 255.0f));
    }

    public ColoredLitInstance color(float red, float green, float blue) {
        return color((byte) (red * 255.0f), (byte) (green * 255.0f), (byte) (blue * 255.0f));
    }

    @Override
    public ColoredLitInstance light(int light) {
        this.light = light;
        return this;
    }
}
