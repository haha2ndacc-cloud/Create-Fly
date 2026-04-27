package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour;
import net.fabricmc.fabric.api.block.v1.FabricBlockState;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ConnectedTextureBehaviour.class)
public class ConnectedTextureBehaviourMixin {
    @Overwrite(remap = false)
    public BlockState getCTBlockState(
        BlockAndTintGetter reader,
        BlockState reference,
        Direction face,
        BlockPos fromPos,
        BlockPos toPos
    ) {
        return ((FabricBlockState) reader.getBlockState(toPos)).getAppearance(reader, toPos, face, reference, fromPos);
    }
}
