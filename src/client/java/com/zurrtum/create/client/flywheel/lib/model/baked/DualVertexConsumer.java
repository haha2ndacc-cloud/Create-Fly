package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jspecify.annotations.Nullable;

public class DualVertexConsumer extends VertexMultiConsumer.Double {
    public DualVertexConsumer(VertexConsumer first, VertexConsumer second) {
        super(first, second);
    }

    @Override
    public void putBulkData(
        PoseStack.Pose pose,
        BakedQuad quad,
        QuadBrightness brightness,
        int color,
        QuadLightmapCoords lightmapCoord,
        int overlayCoords
    ) {
        first.putBulkData(pose, quad, brightness, color, lightmapCoord, overlayCoords);
        second.putBulkData(pose, quad, brightness, color, lightmapCoord, overlayCoords);
    }

    public void emit(
        ModelPart part,
        PoseStack matrices,
        @Nullable TextureAtlasSprite sprite,
        int light,
        int overlay,
        int color
    ) {
        ((ItemMeshEmitter) second).emit(part, matrices, sprite, (ItemMeshEmitter) first, light, overlay, color);
    }
}
