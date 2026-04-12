package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

import java.util.concurrent.CompletableFuture;

public class EntityBlockLightLayer extends AbstractEntityBlockLayer {
    private final Matrix4fc pose;

    public EntityBlockLightLayer(Matrix4fc pose, RenderType type) {
        super(new CompletableFuture<>(), type);
        this.pose = pose;
    }

    private EntityBlockLightLayer(Matrix4fc pose, RenderType type, EntityBlockTemplateMesh template) {
        super(type);
        this.pose = pose;
        positions = template.positions;
        colors = template.colors;
        uvs = template.uvs;
        lights = template.lights;
    }

    public static EntityBlockLightLayer create(Matrix4fc pose, EntityBlockTemplateMesh template, int cardinalLighting) {
        return new EntityBlockLightLayer(pose, template.type.getRenderType(cardinalLighting), template);
    }

    public static EntityBlockLightLayer light(Matrix4fc pose, EntityBlockTemplateMesh template, int cardinalLighting) {
        return new EntityBlockLightLayer(pose, template.type.getLightRenderType(cardinalLighting), template);
    }

    @Override
    Matrix4fc pose() {
        return pose;
    }

    @Override
    public void submit(PoseStack matrices, OrderedSubmitNodeCollector queue) {
        matrices.pushPose();
        matrices.last().pose().mul(pose);
        queue.submitCustomGeometry(matrices, type, this);
        matrices.popPose();
    }

    @Override
    public void submit(Pose transform, PoseStack matrices, OrderedSubmitNodeCollector queue) {
        matrices.pushPose();
        matrices.last().pose().mul(transform.pose()).mul(pose);
        queue.submitCustomGeometry(matrices, type, this);
        matrices.popPose();
    }

    @Override
    public void submit(RenderType type, PoseStack matrices, OrderedSubmitNodeCollector queue) {
        matrices.pushPose();
        matrices.last().pose().mul(pose);
        queue.submitCustomGeometry(matrices, type, this);
        matrices.popPose();
    }

    @Override
    public void renderInto(Pose pose, VertexConsumer buffer) {
        Pose entry = pose.copy();
        entry.pose().mul(this.pose);
        render(entry, buffer);
    }

    @Override
    public void render(Pose pose, VertexConsumer buffer) {
        future.join();
        Matrix4f modelMat = pose.pose();
        Vector4f pos = new Vector4f();
        for (int i = 0, size = positions.length; i < size; i++) {
            positions[i].mul(modelMat, pos);
            buffer.addVertex(
                pos.x(),
                pos.y(),
                pos.z(),
                colors[i],
                uvs[i << 1],
                uvs[(i << 1) + 1],
                0,
                lights[i],
                0,
                0,
                0
            );
        }
    }
}
