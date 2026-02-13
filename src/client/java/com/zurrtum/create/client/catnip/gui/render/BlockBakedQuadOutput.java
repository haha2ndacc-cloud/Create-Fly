package com.zurrtum.create.client.catnip.gui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadBrightness;
import com.mojang.blaze3d.vertex.QuadLightmapCoords;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.BakedQuadOutput;
import net.minecraft.client.renderer.block.model.BakedQuad;

public record BlockBakedQuadOutput(BufferSource bufferSource) implements BakedQuadOutput {
    @Override
    public void put(
        PoseStack.Pose pose,
        BakedQuad quad,
        QuadBrightness brightness,
        int color,
        QuadLightmapCoords lightmapCoord,
        int overlayCoords
    ) {
        bufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(quad.spriteInfo().layer()))
            .putBulkData(pose, quad, brightness, color, lightmapCoord, overlayCoords);
    }
}
