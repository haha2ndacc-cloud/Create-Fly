package com.zurrtum.create.client.infrastructure.particle;

import com.zurrtum.create.infrastructure.particle.CubeParticleData;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class CubeParticle extends Particle {
    protected float scale;
    protected boolean hot;
    protected float red = 1.0F;
    protected float green = 1.0F;
    protected float blue = 1.0F;
    protected float alpha = 1.0F;

    public CubeParticle(
        ClientLevel world,
        CubeParticleData data,
        double x,
        double y,
        double z,
        double motionX,
        double motionY,
        double motionZ
    ) {
        super(world, x, y, z);
        xd = motionX;
        yd = motionY;
        zd = motionZ;

        setColor(data.red(), data.green(), data.blue());
        setScale(data.scale());
        averageAge(data.avgAge());
        setHot(data.hot());
    }

    public void setColor(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public void setScale(float scale) {
        this.scale = scale;
        setSize(scale * 0.5f, scale * 0.5f);
    }

    public void averageAge(int age) {
        lifetime = (int) (age + (random.nextDouble() * 2.0D - 1.0D) * 8);
    }

    public void setHot(boolean hot) {
        this.hot = hot;
    }

    private boolean billowing;

    @Override
    public void tick() {
        if (hot && age > 0) {
            if (yo == y) {
                billowing = true;
                stoppedByCollision = false; // Prevent motion being ignored due to vertical collision
                if (xd == 0 && zd == 0) {
                    Vec3 diff = Vec3.atLowerCornerOf(BlockPos.containing(x, y, z)).add(0.5, 0.5, 0.5).subtract(x, y, z);
                    xd = -diff.x * 0.1;
                    zd = -diff.z * 0.1;
                }
                xd *= 1.1;
                yd *= 0.9;
                zd *= 1.1;
            } else if (billowing) {
                yd *= 1.2;
            }
        }
        super.tick();
    }

    public void extract(QuadParticleRenderState submittable, Camera camera, float tickProgress) {
        Vec3 projectedView = camera.position();
        float lerpedX = (float) (Mth.lerp(tickProgress, xo, x) - projectedView.x());
        float lerpedY = (float) (Mth.lerp(tickProgress, yo, y) - projectedView.y());
        float lerpedZ = (float) (Mth.lerp(tickProgress, zo, z) - projectedView.z());
        double ageMultiplier = 1 - Math.pow(Mth.clamp(age + tickProgress, 0, lifetime), 3) / Math.pow(lifetime, 3);
        float scale = (float) (this.scale * ageMultiplier);
        int color = ARGB.colorFromFloat(alpha, red, green, blue);
        submittable.add(lerpedX, lerpedY, lerpedZ, scale, color);
    }

    @Override
    public ParticleRenderType getGroup() {
        return CubeParticleGroup.SHEET;
    }

    public boolean shouldRender(Frustum frustum) {
        return frustum.pointInFrustum(x, y, z);
    }

    public static class Factory implements ParticleProvider<CubeParticleData> {

        @Override
        public Particle createParticle(
            CubeParticleData data,
            ClientLevel world,
            double x,
            double y,
            double z,
            double motionX,
            double motionY,
            double motionZ,
            RandomSource random
        ) {
            return new CubeParticle(world, data, x, y, z, motionX, motionY, motionZ);
        }
    }
}
