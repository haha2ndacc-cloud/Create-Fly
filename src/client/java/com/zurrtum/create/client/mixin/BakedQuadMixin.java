package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.model.NormalsBakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BakedQuad.class)
public class BakedQuadMixin implements NormalsBakedQuad {
    @Unique
    @Nullable
    private Vector3fc normal0;
    @Unique
    @Nullable
    private Vector3fc normal1;
    @Unique
    @Nullable
    private Vector3fc normal2;
    @Unique
    @Nullable
    private Vector3fc normal3;

    @Override
    public void create$setNormals(Vector3fc normal) {
        normal0 = normal;
        normal1 = normal;
        normal2 = normal;
        normal3 = normal;
    }

    @Override
    public void create$setNormals(Vector3fc normal0, Vector3fc normal1, Vector3fc normal2, Vector3fc normal3) {
        this.normal0 = normal0;
        this.normal1 = normal1;
        this.normal2 = normal2;
        this.normal3 = normal3;
    }

    @Override
    public void create$setNormals(@NotNull NormalsBakedQuad quad) {
        normal0 = quad.create$getNormal0();
        normal1 = quad.create$getNormal1();
        normal2 = quad.create$getNormal2();
        normal3 = quad.create$getNormal3();
    }

    @Override
    @Nullable
    public Vector3fc create$getNormal0() {
        return normal0;
    }

    @Override
    @Nullable
    public Vector3fc create$getNormal1() {
        return normal1;
    }

    @Override
    @Nullable
    public Vector3fc create$getNormal2() {
        return normal2;
    }

    @Override
    @Nullable
    public Vector3fc create$getNormal3() {
        return normal3;
    }

    @Override
    public Vector3fc create$getNormal(int vertex) {
        return switch (vertex) {
            case 0 -> normal0;
            case 1 -> normal1;
            case 2 -> normal2;
            case 3 -> normal3;
            default -> throw new IndexOutOfBoundsException(vertex);
        };
    }
}
