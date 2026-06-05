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
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TerrainParticle.class)
public abstract class TerrainParticleMixin {
    @WrapOperation(method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockStateModelSet;getParticleMaterial(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/resources/model/sprite/Material$Baked;"))
    private static Material.Baked onParticle(
        BlockStateModelSet models,
        BlockState blockState,
        Operation<Material.Baked> original,
        @Local(argsOnly = true) ClientLevel level,
        @Local(argsOnly = true) BlockPos pos
    ) {
        BlockStateModel model = models.get(blockState);
        if (model instanceof WrapperBlockStateModel wrapper && wrapper.needUpdateTerrainParticle()) {
            return wrapper.particleMaterialWithInfo(
                level,
                WrapperBlockStateModel.findPos(level, pos, blockState),
                blockState
            );
        }
        return model.particleMaterial();
    }
}
