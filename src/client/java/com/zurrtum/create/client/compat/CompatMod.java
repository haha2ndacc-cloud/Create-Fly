package com.zurrtum.create.client.compat;

import com.zurrtum.create.client.compat.fabric.PotionRenderHandler;
import net.fabricmc.loader.api.FabricLoader;

public class CompatMod {
    public static void register() {
        if (FabricLoader.getInstance().isModLoaded("fabric-transfer-api-v1")) {
            PotionRenderHandler.register();
        }
//        if (Mods.TRINKETS.isLoaded()) {
//            GoggleTrinketRenderer.register();
//        }
    }
}
