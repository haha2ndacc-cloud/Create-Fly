package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import com.zurrtum.create.client.infrastructure.render.BreakingRenderInfo;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(BlockRenderDispatcher.class)
public class BlockRenderDispatcherMixin implements BreakingRenderInfo {
    @Unique
    private BlockAndTintGetter level;

    @Override
    public void create$setRenderLevel(@NotNull BlockAndTintGetter level) {
        this.level = level;
    }

    @Override
    public void create$clearRenderLevel() {
        level = null;
    }

    @WrapOperation(method = "renderBreakingTexture(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/BlockStateModel;collectParts(Lnet/minecraft/util/RandomSource;Ljava/util/List;)V"))
    private void collectParts(
        BlockStateModel model,
        RandomSource random,
        List<BlockStateModelPart> output,
        Operation<Void> original,
        @Local(argsOnly = true) BlockState state,
        @Local(argsOnly = true) BlockPos pos
    ) {
        if (level != null && WrapperBlockStateModel.unwrapCompat(model) instanceof WrapperBlockStateModel wrapper) {
            wrapper.addPartsWithInfo(level, pos, state, random, output);
        } else {
            original.call(model, random, output);
        }
    }
}
