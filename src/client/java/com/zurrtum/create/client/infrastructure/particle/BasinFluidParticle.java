package com.zurrtum.create.client.infrastructure.particle;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.content.processing.basin.BasinBlock;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.infrastructure.particle.FluidParticleData;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public class BasinFluidParticle extends SingleQuadParticle {
    private final BlockPos basinPos;
    private final @Nullable Vec3 targetPos;
    private final Vec3 centerOfBasin;
    private final float yOffset;
    private final float uo;
    private final float vo;
    private final Layer layer;

    public BasinFluidParticle(
        ClientLevel world,
        TextureAtlasSprite still,
        int tint,
        double x,
        double y,
        double z,
        double vx,
        double vy,
        double vz,
        RandomSource random
    ) {
        super(world, x, y, z, vx, vy, vz, still);
        layer = Layer.bySprite(still);
        gravity = 0;
        rCol = (tint >> 16 & 255) / 255.0F;
        gCol = (tint >> 8 & 255) / 255.0F;
        bCol = (tint & 255) / 255.0F;
        alpha = 0.9F;
        xd = 0;
        yd = 0;
        zd = 0;
        uo = random.nextFloat() * 3.0F;
        vo = random.nextFloat() * 3.0F;
        yOffset = random.nextFloat() * 1 / 32.0f;
        y += yOffset;
        quadSize = 0;
        lifetime = 60;
        Vec3 currentPos = new Vec3(x, y, z);
        basinPos = BlockPos.containing(currentPos);
        centerOfBasin = VecHelper.getCenterOf(basinPos);
        alpha = 0.9F;

        if (vx != 0) {
            lifetime = 20;
            Vec3 centerOf = VecHelper.getCenterOf(basinPos);
            Vec3 diff = currentPos.subtract(centerOf).multiply(1, 0, 1).normalize().scale(0.375);
            targetPos = centerOf.add(diff);
            xo = this.x = centerOfBasin.x;
            zo = this.z = centerOfBasin.z;
        } else {
            targetPos = null;
        }
    }

    @Override
    public void tick() {
        super.tick();
        quadSize = targetPos != null ? Math.max(1 / 32.0f, 1.0f * age / lifetime / 8) :
            1 / 8.0f * (1 - Math.abs(age - lifetime / 2) / (1.0f * lifetime));

        if (age % 2 == 0) {
            if (!level.getBlockState(basinPos).is(AllBlocks.BASIN) && !BasinBlock.isBasin(level, basinPos)) {
                remove();
                return;
            }

            BlockEntity blockEntity = level.getBlockEntity(basinPos);
            if (blockEntity instanceof BasinBlockEntity) {
                float totalUnits = ((BasinBlockEntity) blockEntity).getTotalFluidUnits(0);
                if (totalUnits < 1) {
                    totalUnits = 0;
                }
                float fluidLevel = Mth.clamp(totalUnits / 162000, 0, 1);
                y = 2 / 16.0f + basinPos.getY() + 12 / 16.0f * fluidLevel + yOffset;
            }

        }

        if (targetPos != null) {
            float progess = 1.0f * age / lifetime;
            Vec3 currentPos = centerOfBasin.add(targetPos.subtract(centerOfBasin).scale(progess));
            x = currentPos.x;
            z = currentPos.z;
        }
    }

    @Override
    protected Layer getLayer() {
        return layer;
    }

    @Override
    protected int getLightCoords(float p_189214_1_) {
        return LightCoordsUtil.FULL_BRIGHT;
    }

    @Override
    protected float getU0() {
        return sprite.getU((uo + 1.0F) / 4.0F);
    }

    @Override
    protected float getU1() {
        return sprite.getU(uo / 4.0F);
    }

    @Override
    protected float getV0() {
        return sprite.getV(vo / 4.0F);
    }

    @Override
    protected float getV1() {
        return sprite.getV((vo + 1.0F) / 4.0F);
    }

    @Override
    public void extract(QuadParticleRenderState submittable, Camera info, float pt) {
        Quaternionf rotation = info.rotation();
        Quaternionf prevRotation = new Quaternionf(rotation);
        rotation.set(-1, 0, 0, 1);
        rotation.normalize();
        super.extract(submittable, info, pt);
        rotation.set(0, 0, 0, 1);
        rotation.mul(prevRotation);
    }

    public static class Factory implements ParticleProvider<FluidParticleData> {
        @Override
        @Nullable
        public Particle createParticle(
            FluidParticleData data,
            ClientLevel level,
            double x,
            double y,
            double z,
            double vx,
            double vy,
            double vz,
            RandomSource random
        ) {
            Fluid fluid = data.fluid();
            FluidState state = fluid.defaultFluidState();
            FluidModel model = level.minecraft.getModelManager().getFluidStateModelSet().get(state);
            int tint = AllFluidConfigs.getTint(level, x, y, z, state, model, fluid, data.components());
            return new BasinFluidParticle(level, model.stillMaterial().sprite(), tint, x, y, z, vx, vy, vz, random);
        }
    }
}
