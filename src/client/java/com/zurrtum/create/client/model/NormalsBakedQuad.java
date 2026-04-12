package com.zurrtum.create.client.model;

import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public interface NormalsBakedQuad {
    void create$setNormals(@Nullable Vector3fc normal);

    void create$setNormals(
        @Nullable Vector3fc normal0,
        @Nullable Vector3fc normal1,
        @Nullable Vector3fc normal2,
        @Nullable Vector3fc normal3
    );

    void create$setNormals(NormalsBakedQuad quad);

    @Nullable Vector3fc create$getNormal0();

    @Nullable Vector3fc create$getNormal1();

    @Nullable Vector3fc create$getNormal2();

    @Nullable Vector3fc create$getNormal3();

    @Nullable Vector3fc create$getNormal(int vertex);

    static void setNormals(BakedQuad quad, BakedQuad target) {
        ((NormalsBakedQuad) (Object) quad).create$setNormals((NormalsBakedQuad) (Object) target);
    }
}
