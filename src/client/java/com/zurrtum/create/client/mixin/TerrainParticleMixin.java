package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TerrainParticle.class)
public abstract class TerrainParticleMixin {
    @Unique
    private static final Vec3i[] DIRECTIONS = new Vec3i[]{
        new Vec3i(0, 0, -1),
        new Vec3i(0, 0, 1),
        new Vec3i(-1, 0, 0),
        new Vec3i(1, 0, 0),
        new Vec3i(-1, 0, -1),
        new Vec3i(1, 0, -1),
        new Vec3i(1, 0, 1),
        new Vec3i(-1, 0, 1),
        new Vec3i(0, -1, 0),
        new Vec3i(0, 1, 0),
        new Vec3i(0, -1, -1),
        new Vec3i(0, -1, 1),
        new Vec3i(-1, -1, 0),
        new Vec3i(1, -1, 0),
        new Vec3i(-1, -1, -1),
        new Vec3i(1, -1, -1),
        new Vec3i(1, -1, 1),
        new Vec3i(-1, -1, 1)
    };

    @Unique
    private static BlockPos findPos(ClientLevel world, BlockPos pos, BlockState state) {
        BlockState target = world.getBlockState(pos);
        if (target == state) {
            return pos;
        }
        BlockPos.MutableBlockPos position = pos.mutable();
        for (Vec3i move : DIRECTIONS) {
            target = world.getBlockState(position.setWithOffset(pos, move));
            if (target == state) {
                return position;
            }
        }
        return pos;
    }

    @WrapOperation(method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockStateModelSet;getParticleMaterial(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/resources/model/sprite/Material$Baked;"))
    private static Material.Baked onParticle(
        BlockStateModelSet models,
        BlockState state,
        Operation<Material.Baked> original,
        @Local(argsOnly = true) ClientLevel world,
        @Local(argsOnly = true) BlockPos pos
    ) {
        BlockStateModel model = models.get(state);
        if (WrapperBlockStateModel.unwrapCompat(model) instanceof WrapperBlockStateModel wrapper && wrapper.needUpdateTerrainParticle()) {
            return wrapper.particleMaterialWithInfo(world, findPos(world, pos, state), state);
        } else {
            return model.particleMaterial();
        }
    }
}
