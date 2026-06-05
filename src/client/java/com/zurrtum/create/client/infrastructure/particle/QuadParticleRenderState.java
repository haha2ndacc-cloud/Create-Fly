package com.zurrtum.create.client.infrastructure.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.LightCoordsUtil;
import org.joml.Vector3f;

import java.util.Arrays;

public class QuadParticleRenderState extends net.minecraft.client.renderer.state.level.QuadParticleRenderState {
    public static final Vector3f[] CUBE = {
        // TOP
        new Vector3f(-1, -1, 1), new Vector3f(-1, -1, -1), new Vector3f(1, -1, -1), new Vector3f(1, -1, 1),

        // BOTTOM
        new Vector3f(1, 1, 1), new Vector3f(1, 1, -1), new Vector3f(-1, 1, -1), new Vector3f(-1, 1, 1),

        // FRONT
        new Vector3f(1, 1, -1), new Vector3f(1, -1, -1), new Vector3f(-1, -1, -1), new Vector3f(-1, 1, -1),

        // BACK
        new Vector3f(-1, 1, 1), new Vector3f(-1, -1, 1), new Vector3f(1, -1, 1), new Vector3f(1, 1, 1),

        // LEFT
        new Vector3f(1, 1, 1), new Vector3f(1, -1, 1), new Vector3f(1, -1, -1), new Vector3f(1, 1, -1),

        // RIGHT
        new Vector3f(-1, 1, -1), new Vector3f(-1, -1, -1), new Vector3f(-1, -1, 1), new Vector3f(-1, 1, 1)
    };

    private final CubeStorage particle = new CubeStorage();

    public QuadParticleRenderState() {
        super();
        particles.put(CubeParticleGroup.RENDER_TYPE, particle);
    }

    public void add(float x, float y, float z, float scale, int color) {
        particle.add(x, y, z, scale, color);
        particleCount++;
    }

    @Override
    public void add(
        SingleQuadParticle.Layer layer,
        float x,
        float y,
        float z,
        float xRot,
        float yRot,
        float zRot,
        float wRot,
        float scale,
        float u0,
        float u1,
        float v0,
        float v1,
        int color,
        int lightCoords
    ) {
        add(x, y, z, scale, color);
    }

    @Override
    public void clear() {
        particle.clear();
        particleCount = 0;
    }

    @Override
    public void buildLayer(SingleQuadParticle.Layer layer, VertexConsumer bufferBuilder) {
        Vector3f vectorInstance = new Vector3f();
        particle.forEachParticle((x, y, z, scale, color) -> renderCubeQuad(
            bufferBuilder,
            vectorInstance,
            x,
            y,
            z,
            scale,
            color,
            LightCoordsUtil.FULL_BRIGHT
        ));
    }

    protected void renderCubeQuad(
        VertexConsumer buffer,
        Vector3f vectorInstance,
        float x,
        float y,
        float z,
        float scale,
        int color,
        int light
    ) {
        for (int i = 0; i < 6; i++) {
            // 6 faces to a cube
            for (int j = 0; j < 4; j++) {
                CUBE[i * 4 + j].mul(scale, vectorInstance).add(x, y, z);
                buffer.addVertex(vectorInstance.x, vectorInstance.y, vectorInstance.z).setUv((float) j / 2, j % 2)
                    .setColor(color).setLight(light);
            }
        }
    }

    @FunctionalInterface
    public interface ParticleConsumer {
        void consume(final float x, final float y, final float z, final float scale, final int color);
    }

    public static class CubeStorage extends Storage {
        public void add(final float x, final float y, final float z, final float scale, final int color) {
            if (currentParticleIndex >= capacity) {
                grow();
            }
            int i = currentParticleIndex * 4;
            floatValues[i++] = x;
            floatValues[i++] = y;
            floatValues[i++] = z;
            floatValues[i] = scale;
            intValues[currentParticleIndex * 2] = color;
            currentParticleIndex++;
        }

        public void forEachParticle(ParticleConsumer consumer) {
            for (int particleIndex = 0; particleIndex < currentParticleIndex; particleIndex++) {
                int floatIndex = particleIndex * 4;
                consumer.consume(
                    floatValues[floatIndex++],
                    floatValues[floatIndex++],
                    floatValues[floatIndex++],
                    floatValues[floatIndex],
                    intValues[particleIndex]
                );
            }
        }

        private void grow() {
            capacity *= 2;
            floatValues = Arrays.copyOf(floatValues, capacity * 4);
            intValues = Arrays.copyOf(intValues, capacity);
        }
    }
}
