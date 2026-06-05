package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.jspecify.annotations.Nullable;

public class DualVertexConsumer extends VertexMultiConsumer.Double {
    public DualVertexConsumer(VertexConsumer first, VertexConsumer second) {
        super(first, second);
    }

    @Override
    public void putBlockBakedQuad(float x, float y, float z, BakedQuad quad, QuadInstance instance) {
        first.putBlockBakedQuad(x, y, z, quad, instance);
        second.putBlockBakedQuad(x, y, z, quad, instance);
    }

    @Override
    public void putBakedQuad(PoseStack.Pose pose, BakedQuad quad, QuadInstance instance) {
        first.putBakedQuad(pose, quad, instance);
        second.putBakedQuad(pose, quad, instance);
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
