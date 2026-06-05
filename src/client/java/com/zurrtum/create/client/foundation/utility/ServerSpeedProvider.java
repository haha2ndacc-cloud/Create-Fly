package com.zurrtum.create.client.foundation.utility;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import net.minecraft.client.Minecraft;

public class ServerSpeedProvider {
    public static final LerpedFloat modifier = LerpedFloat.linear();
    public static int clientTimer;
    public static boolean initialized;

    public static void clientTick(Minecraft mc) {
        if (mc.hasSingleplayerServer() && mc.isPaused()) {
            return;
        }
        modifier.tickChaser();
        clientTimer++;
    }

    public static float get() {
        return modifier.getValue();
    }
}
