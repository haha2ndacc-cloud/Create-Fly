package com.zurrtum.create.client.infrastructure.particle;


import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.content.equipment.goggles.GogglesItem;
import com.zurrtum.create.infrastructure.particle.RotationIndicatorParticleData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RotationIndicatorParticle extends SimpleAnimatedParticle {

    protected float radius;
    protected float radius1;
    protected float radius2;
    protected float speed;
    protected Axis axis;
    protected Vec3 origin;
    protected Vec3 offset;

    private RotationIndicatorParticle(
        ClientLevel world,
        double x,
        double y,
        double z,
        int color,
        float radius1,
        float radius2,
        float speed,
        Axis axis,
        int lifeSpan,
        SpriteSet sprite,
        RandomSource random
    ) {
        super(world, x, y, z, sprite, 0);
        xd = 0;
        yd = 0;
        zd = 0;
        origin = new Vec3(x, y, z);
        quadSize *= 0.75F;
        lifetime = lifeSpan + random.nextInt(32);
        setFadeColor(color);
        setColor(Color.mixColors(color, 0xFFFFFF, 0.5f));
        setSpriteFromAge(sprite);
        this.radius1 = radius1;
        radius = radius1;
        this.radius2 = radius2;
        this.speed = speed;
        this.axis = axis;
        offset = axis.isHorizontal() ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        move(0, 0, 0);
        xo = this.x;
        yo = this.y;
        zo = this.z;
    }

    @Override
    public void tick() {
        super.tick();
        radius += (radius2 - radius) * 0.1f;
    }

    @Override
    public void move(double x, double y, double z) {
        float time = AnimationTickHolder.getTicks(level);
        float angle = time * speed % 360 - speed / 2 * age * ((float) age / lifetime);
        if (speed < 0 && axis.isVertical()) {
            angle += 180;
        }
        Vec3 position = VecHelper.rotate(offset.scale(radius), angle, axis).add(origin);
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
    }

    public static class Factory implements ParticleProvider<RotationIndicatorParticleData> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet animatedSprite) {
            spriteSet = animatedSprite;
        }

        @Override
        @Nullable
        public Particle createParticle(
            RotationIndicatorParticleData data,
            ClientLevel worldIn,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            RandomSource random
        ) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (worldIn == mc.level && (player == null || !GogglesItem.isWearingGoggles(player))) {
                return null;
            }
            return new RotationIndicatorParticle(
                worldIn,
                x,
                y,
                z,
                data.color(),
                data.radius1(),
                data.radius2(),
                data.speed(),
                data.axis(),
                data.lifeSpan(),
                spriteSet,
                random
            );
        }
    }

}