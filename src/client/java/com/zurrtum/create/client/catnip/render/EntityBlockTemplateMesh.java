package com.zurrtum.create.client.catnip.render;

import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

public class EntityBlockTemplateMesh {
    public final EntityBlockRenderType type;
    public final int vertexCount;
    public final Vector4fc[] positions;
    public final int[] colors;
    public final float[] uvs;
    public final int[] lights;
    public final Vector3fc[] normals;
    private Vector4fc @Nullable [] lightPositions;

    public EntityBlockTemplateMesh(
        EntityBlockRenderType type,
        int vertexCount,
        Vector4fc[] positions,
        int[] colors,
        float[] uvs,
        int[] lights,
        Vector3fc[] normals
    ) {
        this.type = type;
        this.vertexCount = vertexCount;
        this.positions = positions;
        this.colors = colors;
        this.uvs = uvs;
        this.lights = lights;
        this.normals = normals;
    }

    public Vector4fc[] getLightPositions() {
        if (lightPositions == null) {
            Vector4fc[] data = new Vector4f[vertexCount];
            for (int i = 0; i < vertexCount; i++) {
                Vector4fc position = positions[i];
                data[i] = new Vector4f(
                    (position.x() - 0.5f) * 0.9375f + 0.5f,
                    (position.y() - 0.5f) * 0.9375f + 0.5f,
                    (position.z() - 0.5f) * 0.9375f + 0.5f,
                    1.0f
                );
            }
            lightPositions = data;
        }
        return lightPositions;
    }
}
