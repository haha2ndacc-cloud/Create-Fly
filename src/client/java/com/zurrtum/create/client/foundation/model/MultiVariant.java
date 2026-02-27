package com.zurrtum.create.client.foundation.model;

import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.Material;
import net.minecraft.util.RandomSource;

import java.util.List;

public record MultiVariant(List<BlockModelPart> models, Material.Baked particleMaterial,
                           boolean hasTranslucency) implements BlockStateModel {
    @Override
    public void collectParts(RandomSource random, List<BlockModelPart> output) {
        output.addAll(models);
    }
}
