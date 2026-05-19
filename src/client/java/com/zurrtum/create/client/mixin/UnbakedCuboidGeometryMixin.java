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
import org.joml.GeometryUtils;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(UnbakedCuboidGeometry.class)
public class UnbakedCuboidGeometryMixin {
    @WrapOperation(method = "bake(Ljava/util/List;Lnet/minecraft/client/resources/model/sprite/TextureSlots;Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/client/renderer/block/dispatch/ModelState;Lnet/minecraft/client/resources/model/ModelDebugName;)Lnet/minecraft/client/resources/model/geometry/QuadCollection;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/cuboid/FaceBakery;bakeQuad(Lnet/minecraft/client/resources/model/ModelBaker;Lorg/joml/Vector3fc;Lorg/joml/Vector3fc;Lnet/minecraft/client/resources/model/cuboid/CuboidFace;Lnet/minecraft/client/resources/model/sprite/Material$Baked;Lnet/minecraft/core/Direction;Lnet/minecraft/client/renderer/block/dispatch/ModelState;Lnet/minecraft/client/resources/model/cuboid/CuboidRotation;ZI)Lnet/minecraft/client/resources/model/geometry/BakedQuad;"))
    private static BakedQuad bakeQuad(
        ModelBaker modelBaker,
        Vector3fc from,
        Vector3fc _to,
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
            _to,
            face,
            material,
            facing,
            modelState,
            elementRotation,
            shade,
            lightEmission
        );
        if (((NormalsModelElement) (Object) element).create$calcNormals()) {
            Vector3f normal = new Vector3f();
            GeometryUtils.normal(quad.position0(), quad.position1(), quad.position2(), normal);
            ((NormalsBakedQuad) (Object) quad).create$setNormals(normal);
        }
        return quad;
    }
}
