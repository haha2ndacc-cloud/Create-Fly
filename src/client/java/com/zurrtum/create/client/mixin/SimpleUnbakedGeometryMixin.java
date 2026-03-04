package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.model.NormalsBakedQuad;
import com.zurrtum.create.client.model.NormalsModelElement;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.cuboid.CuboidRotation;
import net.minecraft.client.resources.model.cuboid.UnbakedCuboidGeometry;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(UnbakedCuboidGeometry.class)
public class SimpleUnbakedGeometryMixin {
    @WrapOperation(method = "bake(Ljava/util/List;Lnet/minecraft/client/resources/model/sprite/TextureSlots;Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/client/renderer/block/dispatch/ModelState;Lnet/minecraft/client/resources/model/ModelDebugName;)Lnet/minecraft/client/resources/model/geometry/QuadCollection;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/cuboid/FaceBakery;bakeQuad(Lnet/minecraft/client/resources/model/ModelBaker;Lorg/joml/Vector3fc;Lorg/joml/Vector3fc;Lnet/minecraft/client/resources/model/cuboid/CuboidFace;Lnet/minecraft/client/resources/model/sprite/Material$Baked;Lnet/minecraft/core/Direction;Lnet/minecraft/client/renderer/block/dispatch/ModelState;Lnet/minecraft/client/resources/model/cuboid/CuboidRotation;ZI)Lnet/minecraft/client/resources/model/geometry/BakedQuad;"))
    private static BakedQuad bakeQuad(
        ModelBaker modelBaker,
        Vector3fc from,
        Vector3fc to,
        CuboidFace face,
        Material.Baked material,
        Direction facing,
        ModelState modelState,
        CuboidRotation elementRotation,
        boolean shade,
        int lightEmission,
        Operation<BakedQuad> original,
        @Local CuboidModelElement element
    ) {
        BakedQuad quad = original.call(
            modelBaker,
            from,
            to,
            face,
            material,
            facing,
            modelState,
            elementRotation,
            shade,
            lightEmission
        );
        if (NormalsModelElement.calcNormals(element)) {
            Vector3f v1 = new Vector3f(quad.position3());
            Vector3fc t1 = quad.position1();
            Vector3f v2 = new Vector3f(quad.position2());
            Vector3fc t2 = quad.position0();
            v1.sub(t1);
            v2.sub(t2);
            v2.cross(v1);
            Vector3fc vector = v2.normalize();

            int x = ((byte) Math.round(vector.x() * 127)) & 0xFF;
            int y = ((byte) Math.round(vector.y() * 127)) & 0xFF;
            int z = ((byte) Math.round(vector.z() * 127)) & 0xFF;
            int normal = x | (y << 0x08) | (z << 0x10);
            NormalsBakedQuad.setNormals(quad, new int[]{normal, normal, normal, normal});
        }
        return quad;
    }
}
