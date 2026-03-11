package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.content.kinetics.waterwheel.LargeWaterWheelBlockEntity;
import com.zurrtum.create.content.kinetics.waterwheel.WaterWheelStructuralBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class WaterWheelStructuralModel extends WrapperBlockStateModel {
    public static final WaterWheelStructuralModel INSTANCE = new WaterWheelStructuralModel();

    public static WaterWheelStructuralModel single(BlockState state, UnbakedRoot unbaked) {
        return INSTANCE;
    }

    @Override
    public boolean needUpdateTerrainParticle() {
        return true;
    }

    @Override
    public Material.Baked particleMaterialWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state) {
        BlockPos master = WaterWheelStructuralBlock.getMaster(world, pos, state);
        if (world.getBlockEntity(master) instanceof LargeWaterWheelBlockEntity blockEntity) {
            return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockEntity.material)
                .particleMaterial();
        }
        return particleMaterial();
    }

    @Override
    public Material.Baked particleMaterial() {
        return Minecraft.getInstance().getModelManager().getBlockStateModelSet().missingModel().particleMaterial();
    }

    @Override
    public int materialFlags() {
        return 0;
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
    }
}
