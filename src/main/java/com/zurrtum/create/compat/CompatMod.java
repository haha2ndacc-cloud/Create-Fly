package com.zurrtum.create.compat;

import com.zurrtum.create.compat.fabric.RecipeCommonPlugin;

public class CompatMod {
    public static void register() {
        if (Mods.JEI.isLoaded() || Mods.RRV.isLoaded()) {
            RecipeCommonPlugin.register();
        }
        //        if (Mods.TRINKETS.isLoaded()) {
        //            GoggleTrinket.register();
        //        }
        //        if (Mods.COMPUTERCRAFT.isLoaded()) {
        //            AllComputerPeripherals.register();
        //            AllComputerDisplaySource.register();
        //        }
    }
}
