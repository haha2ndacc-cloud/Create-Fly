package com.zurrtum.create.client.mixin;

import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WrapperBlockStateModel.class)
public interface WrapperBlockStateModelAccessor {
    @Accessor(value = "wrapped", remap = false)
    BlockStateModel getWrapped();
}
