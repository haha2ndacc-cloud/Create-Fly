package com.zurrtum.create.client.catnip.render;

import net.minecraft.client.renderer.rendertype.RenderType;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix4fc;
import org.joml.Vector4fc;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractEntityBlockLayer implements SuperByteBufferRenderState {
    private static final CompletableFuture<Void> DONE = CompletableFuture.completedFuture(null);
    public final CompletableFuture<Void> future;
    public final RenderType type;
    public Vector4fc @UnknownNullability [] positions;
    public int @UnknownNullability [] colors;
    public float @UnknownNullability [] uvs;
    public int @UnknownNullability [] lights;

    public AbstractEntityBlockLayer(CompletableFuture<Void> future, RenderType type) {
        this.future = future;
        this.type = type;
    }

    public AbstractEntityBlockLayer(RenderType type) {
        future = DONE;
        this.type = type;
    }

    abstract Matrix4fc pose();
}
