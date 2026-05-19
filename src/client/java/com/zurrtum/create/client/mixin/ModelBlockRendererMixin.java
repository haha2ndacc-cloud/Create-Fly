package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zurrtum.create.client.flywheel.lib.model.baked.VanillinMeshEmitterManager;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {
    @WrapOperation(method = "tesselateBlock(Lnet/minecraft/client/renderer/block/BlockQuadOutput;FFFLnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;J)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"))
    private int getLuminance(
        BlockState state,
        Operation<Integer> original,
        @Local(argsOnly = true) BlockAndTintGetter level,
        @Local(argsOnly = true) BlockPos pos
    ) {
        if (state.getBlock() instanceof CopycatBlock block) {
            return block.getLuminance(level, pos);
        }
        return original.call(state);
    }

    @Inject(method = "tesselateFlat(Lnet/minecraft/client/renderer/block/BlockQuadOutput;FFFLjava/util/List;Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", at = @At("HEAD"))
    private void initLightState(
        BlockQuadOutput output,
        float x,
        float y,
        float z,
        List<BlockStateModelPart> parts,
        BlockAndTintGetter level,
        BlockState state,
        BlockPos pos,
        CallbackInfo ci,
        @Share("lightState") LocalRef<BlockState> lightState
    ) {
        lightState.set(state.getBlock() instanceof CopycatBlock ? CopycatBlock.getMaterial(level, pos) : state);
    }

    @ModifyArg(method = "tesselateFlat(Lnet/minecraft/client/renderer/block/BlockQuadOutput;FFFLjava/util/List;Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockModelLighter;getLightCoords(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I"))
    private BlockState getLightState(BlockState state, @Share("lightState") LocalRef<BlockState> lightState) {
        return lightState.get();
    }

    @Inject(method = "tesselateBlock(Lnet/minecraft/client/renderer/block/BlockQuadOutput;FFFLnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;J)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;tesselateAmbientOcclusion(Lnet/minecraft/client/renderer/block/BlockQuadOutput;FFFLjava/util/List;Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V"))
    private void tesselateAmbientOcclusion(
        BlockQuadOutput output,
        float x,
        float y,
        float z,
        BlockAndTintGetter level,
        BlockPos pos,
        BlockState blockState,
        BlockStateModel model,
        long seed,
        CallbackInfo ci
    ) {
        if (output instanceof VanillinMeshEmitterManager meshEmitter) {
            meshEmitter.prepareForModelLayer(true);
        }
    }

    @Inject(method = "tesselateBlock(Lnet/minecraft/client/renderer/block/BlockQuadOutput;FFFLnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;J)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;tesselateFlat(Lnet/minecraft/client/renderer/block/BlockQuadOutput;FFFLjava/util/List;Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V"))
    private void tesselateFlat(
        BlockQuadOutput output,
        float x,
        float y,
        float z,
        BlockAndTintGetter level,
        BlockPos pos,
        BlockState blockState,
        BlockStateModel model,
        long seed,
        CallbackInfo ci
    ) {
        if (output instanceof VanillinMeshEmitterManager meshEmitter) {
            meshEmitter.prepareForModelLayer(false);
        }
    }

    @WrapOperation(method = "tesselateBlock(Lnet/minecraft/client/renderer/block/BlockQuadOutput;FFFLnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;J)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;collectParts(Lnet/minecraft/util/RandomSource;Ljava/util/List;)V"))
    private void collectParts(
        BlockStateModel model,
        RandomSource random,
        List<BlockStateModelPart> output,
        Operation<Void> original,
        @Local(argsOnly = true) BlockAndTintGetter level,
        @Local(argsOnly = true) BlockPos pos,
        @Local(argsOnly = true) BlockState blockState
    ) {
        if (model instanceof WrapperBlockStateModel wrapper) {
            wrapper.addPartsWithInfo(level, pos, blockState, random, output);
        } else {
            original.call(model, random, output);
        }
    }
}
