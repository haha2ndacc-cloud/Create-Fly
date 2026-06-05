package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.content.kinetics.waterwheel.LargeWaterWheelBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class LargeWaterWheelModel extends WrapperBlockStateModel {
    public LargeWaterWheelModel(BlockState state, UnbakedRoot unbaked) {
        super(state, unbaked);
    }

    @Override
    public boolean needUpdateTerrainParticle() {
        return true;
    }

    @Override
    public Material.Baked particleMaterialWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state) {
        if (world.getBlockEntity(pos) instanceof LargeWaterWheelBlockEntity blockEntity) {
            return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockEntity.material)
                .particleMaterial();
        }
        return model.particleMaterial();
    }

    @Override
    public void addPartsWithInfo(
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState state,
        RandomSource random,
        List<BlockStateModelPart> parts
    ) {
    }
}
