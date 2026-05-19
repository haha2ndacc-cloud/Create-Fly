package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zurrtum.create.client.infrastructure.render.BreakingRenderStateInfo;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel.getBlockDestroyModel;

@Mixin(LevelExtractor.class)
public class LevelExtractorMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private @Nullable ClientLevel level;

    @Inject(method = "extractBlockDestroyAnimation(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V", at = @At("HEAD"))
    private void init(
        Camera camera,
        LevelRenderState levelRenderState,
        CallbackInfo ci,
        @Share("models") LocalRef<BlockStateModelSet> ref
    ) {
        ref.set(minecraft.getModelManager().getBlockStateModelSet());
    }

    @ModifyArg(method = "extractBlockDestroyAnimation(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private <E> E addInfo(E e, @Share("models") LocalRef<BlockStateModelSet> ref) {
        BlockBreakingRenderState state = (BlockBreakingRenderState) e;
        BlockState blockState = state.blockState();
        ((BreakingRenderStateInfo) e).create$setRenderModel(getBlockDestroyModel(
            ref.get().get(blockState),
            level,
            state.blockPos(),
            blockState
        ));
        return e;
    }
}
