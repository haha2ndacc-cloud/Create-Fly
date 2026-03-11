package com.zurrtum.create.client.foundation.model;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.RandomSource;

import java.util.List;

public record MultiVariant(List<BlockStateModelPart> models, Material.Baked particleMaterial,
                           int materialFlags) implements BlockStateModel {
    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
        output.addAll(models);
    }
}
