package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.infrastructure.render.BreakingRenderStateInfo;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBreakingRenderState.class)
public class BlockBreakingRenderStateMixin implements BreakingRenderStateInfo {
    @Unique
    private ClientLevel level;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(ClientLevel level, BlockPos pos, int progress, CallbackInfo ci) {
        this.level = level;
    }

    @Override
    public @NonNull BlockAndTintGetter create$getRenderLevel() {
        return level;
    }
}
