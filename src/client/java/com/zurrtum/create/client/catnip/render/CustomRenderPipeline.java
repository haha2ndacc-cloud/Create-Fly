package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.systems.RenderSystem.AutoStorageIndexBuffer;
import org.jspecify.annotations.Nullable;

public interface CustomRenderPipeline {
    void create$updateSequential();

    @Nullable AutoStorageIndexBuffer create$getSequentialBuffer();
}
