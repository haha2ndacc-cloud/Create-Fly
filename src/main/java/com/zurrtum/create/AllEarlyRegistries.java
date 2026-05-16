package com.zurrtum.create;

import com.zurrtum.create.api.registry.CreateRegisterPlugin;

public class AllEarlyRegistries implements CreateRegisterPlugin {
    @Override
    public void onBlockRegister() {
        AllBlockIds.init();
        AllBlockItemIds.init();
        AllBlocks.init();
    }

    @Override
    public void onFluidRegister() {
        AllFluidIds.init();
        AllFluidEntries.init();
        AllFluids.init();
    }

    @Override
    public void onDataLoaderRegister() {
        AllDynamicRegistries.register();
    }

    @Override
    public void onEntityAttributeRegister() {
        AllEntityAttributes.register();
    }
}
