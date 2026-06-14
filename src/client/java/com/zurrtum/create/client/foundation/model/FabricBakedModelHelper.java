package com.zurrtum.create.client.foundation.model;

import com.zurrtum.create.catnip.math.VecHelper;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import static com.zurrtum.create.client.catnip.render.SpriteShiftEntry.getUnInterpolatedU;
import static com.zurrtum.create.client.catnip.render.SpriteShiftEntry.getUnInterpolatedV;

public class FabricBakedModelHelper {
    private static void updateSpriteUv(
        MutableQuadView quad,
        Vec3 diff,
        int i,
        Vec3 uAxis,
        Vec3 vAxis,
        float uScale,
        float vScale,
        TextureAtlasSprite sprite
    ) {
        if (diff.lengthSqr() == 0) {
            return;
        }
        quad.uv(
            i,
            sprite.getU(getUnInterpolatedU(sprite, quad.u(i)) + (float) uAxis.dot(diff) * uScale),
            sprite.getV(getUnInterpolatedV(sprite, quad.v(i)) + (float) vAxis.dot(diff) * vScale)
        );
    }

    public static void cropAndMove(MutableQuadView quad, TextureAtlasSprite sprite, AABB crop, Vec3 move) {
        Vec3 xyz0 = new Vec3(quad.x(0), quad.y(0), quad.z(0));
        Vec3 xyz1 = new Vec3(quad.x(1), quad.y(1), quad.z(1));
        Vec3 xyz2 = new Vec3(quad.x(2), quad.y(2), quad.z(2));
        Vec3 xyz3 = new Vec3(quad.x(3), quad.y(3), quad.z(3));

        Vec3 uAxis = xyz3.add(xyz2).scale(.5);
        Vec3 vAxis = xyz1.add(xyz2).scale(.5);
        Vec3 center = xyz3.add(xyz2).add(xyz0).add(xyz1).scale(.25);

        float u0 = quad.u(0);
        float v0 = quad.v(0);
        float uScale = (float) ((getUnInterpolatedU(sprite, quad.u(3)) - getUnInterpolatedU(
            sprite,
            u0
        )) / xyz3.distanceTo(xyz0));
        float vScale = (float) ((getUnInterpolatedV(sprite, quad.v(1)) - getUnInterpolatedV(
            sprite,
            v0
        )) / xyz1.distanceTo(xyz0));
        if (uScale == 0) {
            uAxis = xyz1.add(xyz2).scale(.5);
            vAxis = xyz3.add(xyz2).scale(.5);
            uScale = (float) ((getUnInterpolatedU(sprite, quad.u(1)) - getUnInterpolatedU(
                sprite,
                u0
            )) / xyz1.distanceTo(xyz0));
            vScale = (float) ((getUnInterpolatedV(sprite, quad.v(3)) - getUnInterpolatedV(
                sprite,
                v0
            )) / xyz3.distanceTo(xyz0));
        }

        uAxis = uAxis.subtract(center).normalize();
        vAxis = vAxis.subtract(center).normalize();

        Vec3 min = new Vec3(crop.minX, crop.minY, crop.minZ);
        Vec3 max = new Vec3(crop.maxX, crop.maxY, crop.maxZ);
        Vec3 newXyz0 = VecHelper.componentMin(max, VecHelper.componentMax(xyz0, min));
        quad.pos(0, newXyz0.add(move).toVector3f());
        updateSpriteUv(quad, newXyz0.subtract(xyz0), 0, uAxis, vAxis, uScale, vScale, sprite);
        Vec3 newXyz1 = VecHelper.componentMin(max, VecHelper.componentMax(xyz1, min));
        quad.pos(1, newXyz1.add(move).toVector3f());
        updateSpriteUv(quad, newXyz1.subtract(xyz1), 1, uAxis, vAxis, uScale, vScale, sprite);
        Vec3 newXyz2 = VecHelper.componentMin(max, VecHelper.componentMax(xyz2, min));
        quad.pos(2, newXyz2.add(move).toVector3f());
        updateSpriteUv(quad, newXyz2.subtract(xyz2), 2, uAxis, vAxis, uScale, vScale, sprite);
        Vec3 newXyz3 = VecHelper.componentMin(max, VecHelper.componentMax(xyz3, min));
        quad.pos(3, newXyz3.add(move).toVector3f());
        updateSpriteUv(quad, newXyz3.subtract(xyz3), 3, uAxis, vAxis, uScale, vScale, sprite);
    }

    public static void updateSpriteUv(
        MutableQuadView quad,
        int i,
        TextureAtlasSprite sprite,
        TextureAtlasSprite newSprite
    ) {
        quad.uv(
            i,
            newSprite.getU(getUnInterpolatedU(sprite, quad.u(i))),
            newSprite.getV(getUnInterpolatedV(sprite, quad.v(i)))
        );
    }
}
