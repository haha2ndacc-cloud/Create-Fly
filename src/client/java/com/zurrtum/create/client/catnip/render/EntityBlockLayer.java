package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.*;

import java.util.concurrent.CompletableFuture;

public class EntityBlockLayer extends AbstractEntityBlockLayer {
    private final Pose pose;
    private final int overlay;
    private final Vector3fc[] normals;

    public EntityBlockLayer(Pose pose, RenderType type, Vector3fc[] normals, int overlay) {
        super(new CompletableFuture<>(), type);
        this.pose = pose;
        this.overlay = overlay;
        this.normals = normals;
    }

    private EntityBlockLayer(Pose pose, RenderType type, EntityBlockTemplateMesh template, int overlay) {
        super(type);
        this.pose = pose;
        positions = template.positions;
        colors = template.colors;
        uvs = template.uvs;
        lights = template.lights;
        this.overlay = overlay;
        normals = template.normals;
    }

    public static EntityBlockLayer create(
        Pose pose,
        EntityBlockTemplateMesh template,
        int overlay,
        int cardinalLighting
    ) {
        return new EntityBlockLayer(pose, template.type.getRenderType(cardinalLighting), template, overlay);
    }

    @Override
    Matrix4fc pose() {
        return pose.pose();
    }

    @Override
    public void submit(PoseStack matrices, OrderedSubmitNodeCollector queue) {
        matrices.pushPose();
        SuperByteBuffer.mul(matrices.last(), pose);
        queue.submitCustomGeometry(matrices, type, this);
        matrices.popPose();
    }

    @Override
    public void submit(Pose transform, PoseStack matrices, OrderedSubmitNodeCollector queue) {
        matrices.pushPose();
        Pose entry = matrices.last();
        SuperByteBuffer.mul(entry, transform);
        SuperByteBuffer.mul(entry, pose);
        queue.submitCustomGeometry(matrices, type, this);
        matrices.popPose();
    }

    @Override
    public void submit(RenderType type, PoseStack matrices, OrderedSubmitNodeCollector queue) {
        matrices.pushPose();
        SuperByteBuffer.mul(matrices.last(), pose);
        queue.submitCustomGeometry(matrices, type, this);
        matrices.popPose();
    }

    @Override
    public void renderInto(Pose pose, VertexConsumer buffer) {
        Pose entry = pose.copy();
        SuperByteBuffer.mul(entry, pose);
        render(entry, buffer);
    }

    @Override
    public void render(Pose pose, VertexConsumer buffer) {
        future.join();
        Matrix4f modelMat = pose.pose();
        Vector4f pos = new Vector4f();
        Vector3f normal = new Vector3f();
        for (int i = 0, size = positions.length; i < size; i++) {
            positions[i].mul(modelMat, pos);
            pose.transformNormal(normals[i], normal);
            buffer.addVertex(
                pos.x(),
                pos.y(),
                pos.z(),
                colors[i],
                uvs[i << 1],
                uvs[(i << 1) + 1],
                overlay,
                lights[i],
                normal.x(),
                normal.y(),
                normal.z()
            );
        }
    }
}
