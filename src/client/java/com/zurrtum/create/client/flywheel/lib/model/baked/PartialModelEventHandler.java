package com.zurrtum.create.client.flywheel.lib.model.baked;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.resources.Identifier;

import java.util.Map;

public final class PartialModelEventHandler {
    private PartialModelEventHandler() {
    }

    public static Map<Identifier, PartialModel> getRegisterAdditional() {
        return PartialModel.ALL;
    }

    public static void onBakingCompleted(PartialModel partial, BlockStateModel bakedModel) {
        partial.blockStateModel = bakedModel;
    }

    public static void onBakingCompleted(Map<Identifier, BlockStateModel> models) {
        PartialModel.populateOnInit = true;
    }
}
