package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.flywheel.backend.engine.uniform.GameTimeHolder;
import net.minecraft.client.multiplayer.ClientLevel.ClientLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevelData.class)
public class ClientLevelDataMixin implements GameTimeHolder {
    @Shadow
    private long gameTime;
    @Unique
    private long joinTime;

    @Inject(method = "setGameTime(J)V", at = @At("HEAD"))
    private void initGameTime(long time, CallbackInfo ci) {
        if (gameTime == 0) {
            joinTime = time;
        }
    }

    @Override
    public int flywheel$ticks() {
        return (int) (gameTime - joinTime);
    }
}
