package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadBrightness;
import com.mojang.blaze3d.vertex.QuadLightmapCoords;
import net.minecraft.client.renderer.block.BakedQuadOutput;
import net.minecraft.client.renderer.block.model.BakedQuad;

public class VanillinMeshEmitterManager extends MeshEmitterManager<MeshEmitter> implements BakedQuadOutput {
    private boolean useAo;

    VanillinMeshEmitterManager() {
        super(MeshEmitter::new);
    }

    public void prepareForModelLayer(boolean useAo) {
        this.useAo = useAo;
    }

    @Override
    public void put(
        PoseStack.Pose pose,
        BakedQuad quad,
        QuadBrightness brightness,
        int color,
        QuadLightmapCoords lightmapCoord,
        int overlayCoords
    ) {
        BufferBuilder buffer = getBuffer(quad.spriteInfo().layer(), quad.shade(), useAo);
        if (buffer != null) {
            buffer.putBulkData(pose, quad, brightness, color, lightmapCoord, overlayCoords);
        }
    }
}
