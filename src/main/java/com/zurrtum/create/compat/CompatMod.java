package com.zurrtum.create.compat;

import com.zurrtum.create.compat.computercraft.AllComputerDisplaySource;
import com.zurrtum.create.compat.computercraft.AllComputerPeripherals;
import com.zurrtum.create.compat.computercraft.ComputerIntegration;
import com.zurrtum.create.compat.fabric.RecipeCommonPlugin;

public class CompatMod {
    public static void register() {
        if (Mods.JEI.isLoaded() || Mods.RRV.isLoaded()) {
            RecipeCommonPlugin.register();
        }
        if (Mods.COMPUTERCRAFT.isLoaded()) {
            ComputerIntegration.register();
            AllComputerPeripherals.register();
            AllComputerDisplaySource.register();
        }
    }
}
