package com.zurrtum.create.client.infrastructure.particle;

import com.zurrtum.create.AllFluids;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.infrastructure.particle.FluidParticleData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

public class FluidParticle extends SingleQuadParticle {
    private final @Nullable ColorParticleOption evaporateParticle;
    private final int lightEmission;
    private final float uo;
    private final float vo;
    private final Layer layer;

    public FluidParticle(
        ClientLevel world,
        TextureAtlasSprite still,
        int tint,
        int lightEmission,
        @Nullable ColorParticleOption evaporateParticle,
        double x,
        double y,
        double z,
        double vx,
        double vy,
        double vz,
        RandomSource random
    ) {
        super(world, x, y, z, vx, vy, vz, still);
        this.lightEmission = lightEmission;
        this.evaporateParticle = evaporateParticle;

        layer = Layer.bySprite(still);
        gravity = 1.0F;
        rCol = 0.8F * (tint >> 16 & 255) / 255.0F;
        gCol = 0.8F * (tint >> 8 & 255) / 255.0F;
        bCol = 0.8F * (tint & 255) / 255.0F;

        xd = vx;
        yd = vy;
        zd = vz;

        quadSize /= 2.0F;
        uo = random.nextFloat() * 3.0F;
        vo = random.nextFloat() * 3.0F;
    }

    @Override
    protected int getLightCoords(float a) {
        int brightnessForRender = super.getLightCoords(a);
        int skyLight = brightnessForRender >> 20;
        int blockLight = brightnessForRender >> 4 & 0xf;
        blockLight = Math.max(blockLight, lightEmission);
        return skyLight << 20 | blockLight << 4;
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
    public void tick() {
        super.tick();
        if (evaporateParticle == null) {
            return;
        }
        if (onGround) {
            remove();
        }
        if (!removed) {
            return;
        }
        if (!onGround && random.nextFloat() < 1 / 8.0f) {
            return;
        }
        level.addParticle(evaporateParticle, x, y, z, 0, 0, 0);
    }

    @Override
    protected Layer getLayer() {
        return layer;
    }

    public static class Factory implements ParticleProvider<FluidParticleData> {
        @Override
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
            BlockState blockState = state.createLegacyBlock();
            FluidModel model = level.minecraft.getModelManager().getFluidStateModelSet().get(state);
            int tint = AllFluidConfigs.getTint(level, x, y, z, blockState, model, fluid, data.components());
            ColorParticleOption evaporateParticle =
                fluid == AllFluids.POTION ? ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, tint | 0xFF000000) :
                    null;
            return new FluidParticle(
                level,
                model.stillMaterial().sprite(),
                tint,
                blockState.getLightEmission(),
                evaporateParticle,
                x,
                y,
                z,
                vx,
                vy,
                vz,
                random
            );
        }
    }
}
