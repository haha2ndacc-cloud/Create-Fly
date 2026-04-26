package com.zurrtum.create.client.catnip.render;

import net.minecraft.client.renderer.rendertype.RenderType;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractEntityBlockLayer implements SuperByteBufferRenderState {
    protected static final CompletableFuture<Void> DONE = CompletableFuture.completedFuture(null);
    protected static final Vector4f pos = new Vector4f();
    protected static final Vector3f normal = new Vector3f();
    protected boolean recycle;
    public @Nullable CompletableFuture<Void> future;
    public @UnknownNullability RenderType type;
    public @UnknownNullability EntityBlockTemplateMesh template;
    public Vector4fc @UnknownNullability [] positions;
    public int @UnknownNullability [] colors;
    public float @UnknownNullability [] uvs;
    public int @UnknownNullability [] lights;

    abstract Matrix4fc pose();
}
