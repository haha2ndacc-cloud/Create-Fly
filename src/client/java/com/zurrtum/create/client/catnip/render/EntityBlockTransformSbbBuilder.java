package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.QuadInstance;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;

public class EntityBlockTransformSbbBuilder extends EntityBlockSbbBuilder {
    private final Pose origin = new Pose();
    private final Pose target = new Pose();

    public EntityBlockTransformSbbBuilder wrap(Pose pose) {
        origin.set(pose);
        return this;
    }

    @Override
    public void put(float x, float y, float z, BakedQuad quad, QuadInstance instance) {
        MaterialInfo info = quad.materialInfo();
        TemplateMeshBuffer buffer = buffers[info.shade() ? info.layer().ordinal() : info.layer().ordinal() + 3];
        if (x != 0 || y != 0 || z != 0) {
            target.set(origin);
            target.translate(x, y, z);
            buffer.putBakedQuad(target, quad, instance);
        } else {
            buffer.putBakedQuad(origin, quad, instance);
        }
    }
}
