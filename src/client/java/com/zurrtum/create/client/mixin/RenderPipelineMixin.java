package com.zurrtum.create.client.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.RenderSystem.AutoStorageIndexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.zurrtum.create.client.catnip.render.CustomRenderPipeline;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RenderPipeline.class)
public class RenderPipelineMixin implements CustomRenderPipeline {
    @Shadow
    @Final
    private Mode vertexFormatMode;
    @Unique
    private boolean quads = true;

    @Override
    public void create$updateSequential() {
        quads = vertexFormatMode == Mode.QUADS;
    }

    @Override
    public AutoStorageIndexBuffer create$getSequentialBuffer() {
        if (quads) {
            return null;
        }
        return RenderSystem.getSequentialBuffer(vertexFormatMode);
    }
}
