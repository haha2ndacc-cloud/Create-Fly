package com.zurrtum.create.client.infrastructure.particle;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.fan.AirCurrent;
import com.zurrtum.create.content.kinetics.fan.IAirCurrentSource;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingType;
import com.zurrtum.create.infrastructure.particle.AirFlowParticleData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class AirFlowParticle extends SimpleAnimatedParticle {

    private final IAirCurrentSource source;
    private final Access access = new Access();

    protected AirFlowParticle(
        ClientLevel world,
        IAirCurrentSource source,
        double x,
        double y,
        double z,
        SpriteSet sprite,
        RandomSource random
    ) {
        super(world, x, y, z, sprite, random.nextFloat() * 0.5f);
        this.source = source;
        quadSize *= 0.75F;
        lifetime = 40;
        hasPhysics = false;
        selectSprite(7);
        Vec3 offset = VecHelper.offsetRandomly(Vec3.ZERO, random, 0.25f);
        setPos(x + offset.x, y + offset.y, z + offset.z);
        xo = this.x;
        yo = this.y;
        zo = this.z;
        setColor(0xEEEEEE);
        setAlpha(0.25f);
    }

    @Override
    public void tick() {
        if (source == null || source.isSourceRemoved()) {
            remove();
            return;
        }
        xo = x;
        yo = y;
        zo = z;
        if (age++ >= lifetime) {
            remove();
        } else {
            AirCurrent airCurrent = source.getAirCurrent();
            if (airCurrent == null || !airCurrent.bounds.inflate(0.25f).contains(x, y, z)) {
                remove();
                return;
            }

            Vec3 directionVec = Vec3.atLowerCornerOf(airCurrent.direction.getUnitVec3i());
            Vec3 motion = directionVec.scale(1 / 8.0f);
            if (!source.getAirCurrent().pushing) {
                motion = motion.scale(-1);
            }

            double distance = new Vec3(x, y, z).subtract(VecHelper.getCenterOf(source.getAirCurrentPos()))
                .multiply(directionVec).length() - 0.5f;
            if (distance > airCurrent.maxDistance + 1 || distance < -0.25f) {
                remove();
                return;
            }
            motion = motion.scale(airCurrent.maxDistance - (distance - 1.0f)).scale(0.5f);

            FanProcessingType type = getType(distance);
            if (type == null) {
                setColor(0xEEEEEE);
                setAlpha(0.25f);
                selectSprite((int) Mth.clamp(distance / airCurrent.maxDistance * 8 + random.nextInt(4), 0, 7));
            } else {
                type.morphAirFlow(access, random);
                selectSprite(random.nextInt(3));
            }

            xd = motion.x;
            yd = motion.y;
            zd = motion.z;

            if (onGround) {
                xd *= 0.7;
                zd *= 0.7;
            }
            move(xd, yd, zd);
        }
    }

    @Nullable
    private FanProcessingType getType(double distance) {
        if (source.getAirCurrent() == null) {
            return null;
        }
        return source.getAirCurrent().getTypeAt((float) distance);
    }

    @Override
    public int getLightCoords(float partialTick) {
        BlockPos blockpos = BlockPos.containing(x, y, z);
        return level.isLoaded(blockpos) ? LightCoordsUtil.getLightCoords(level, blockpos) : 0;
    }

    private void selectSprite(int index) {
        setSprite(sprites.get(index, 8));
    }

    public static class Factory implements ParticleProvider<AirFlowParticleData> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet animatedSprite) {
            spriteSet = animatedSprite;
        }

        @Override
        public Particle createParticle(
            AirFlowParticleData data,
            ClientLevel worldIn,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            RandomSource random
        ) {
            BlockEntity be = worldIn.getBlockEntity(new BlockPos(data.posX(), data.posY(), data.posZ()));
            if (!(be instanceof IAirCurrentSource)) {
                be = null;
            }
            return new AirFlowParticle(worldIn, (IAirCurrentSource) be, x, y, z, spriteSet, random);
        }
    }

    private class Access implements FanProcessingType.AirFlowParticleAccess {
        @Override
        public void setColor(int color) {
            AirFlowParticle.this.setColor(color);
        }

        @Override
        public void setAlpha(float alpha) {
            AirFlowParticle.this.setAlpha(alpha);
        }

        @Override
        public void spawnExtraParticle(ParticleOptions options, float speedMultiplier) {
            level.addParticle(options, x, y, z, xd * speedMultiplier, yd * speedMultiplier, zd * speedMultiplier);
        }
    }

}