package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;

public class EntityBlockLayer extends AbstractEntityBlockLayer {
    private static final Deque<EntityBlockLayer> pool = new ArrayDeque<>();
    private static int capacity = 16, index;
    private static @UnknownNullability EntityBlockLayer[] used = new EntityBlockLayer[capacity];
    private final Pose pose = new Pose();
    private int overlay;
    private @UnknownNullability Vector3fc[] normals;

    public static void recycleAll() {
        for (int i = 0; i < index; i++) {
            used[i].recycle();
        }
        index = 0;
    }

    public static void clear() {
        pool.clear();
        index = 0;
        for (int i = 0; i < capacity; i++) {
            used[i] = null;
        }
    }

    @Override
    public void recycle() {
        template.recycle(colors, uvs, lights);
        pool.addLast(this);
    }

    public static EntityBlockLayer create(
        Pose pose,
        EntityBlockTemplateMesh template,
        int overlay,
        int cardinalLighting,
        boolean recycle
    ) {
        EntityBlockLayer layer = pool.pollFirst();
        if (layer == null) {
            layer = new EntityBlockLayer();
        }
        layer.recycle = recycle;
        layer.future = new CompletableFuture<>();
        layer.type = template.type.getRenderType(cardinalLighting);
        layer.template = template;
        layer.pose.set(pose);
        layer.overlay = overlay;
        layer.normals = template.normals;
        return layer;
    }

    public static EntityBlockLayer resolve(
        Pose pose,
        EntityBlockTemplateMesh template,
        int overlay,
        int cardinalLighting,
        boolean recycle
    ) {
        EntityBlockLayer layer = pool.pollFirst();
        if (layer == null) {
            layer = new EntityBlockLayer();
        }
        layer.recycle = recycle;
        layer.future = DONE;
        layer.type = template.type.getRenderType(cardinalLighting);
        layer.template = template;
        layer.pose.set(pose);
        layer.positions = template.positions;
        layer.colors = template.colors;
        layer.uvs = template.uvs;
        layer.lights = template.lights;
        layer.overlay = overlay;
        layer.normals = template.normals;
        return layer;
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
        if (future != null) {
            future.join();
            future = null;
            if (recycle) {
                if (index == capacity) {
                    capacity <<= 1;
                    EntityBlockLayer[] old = used;
                    used = new EntityBlockLayer[capacity];
                    System.arraycopy(old, 0, used, 0, index);
                }
                used[index++] = this;
            }
        }
        Matrix4f modelMat = pose.pose();
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
