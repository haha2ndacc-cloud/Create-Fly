package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;

public class EntityBlockLightLayer extends AbstractEntityBlockLayer {
    private static final Deque<EntityBlockLightLayer> pool = new ArrayDeque<>();
    private static int capacity = 16, index;
    private static @UnknownNullability EntityBlockLightLayer[] used = new EntityBlockLightLayer[capacity];
    private final Matrix4f pose = new Matrix4f();

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

    public static EntityBlockLightLayer create(
        Pose pose,
        EntityBlockTemplateMesh template,
        int cardinalLighting,
        boolean recycle
    ) {
        EntityBlockLightLayer layer = pool.pollFirst();
        if (layer == null) {
            layer = new EntityBlockLightLayer();
        }
        layer.recycle = recycle;
        layer.future = new CompletableFuture<>();
        layer.type = template.type.getRenderType(cardinalLighting);
        layer.template = template;
        layer.pose.set(pose.pose());
        return layer;
    }

    public static EntityBlockLightLayer createLight(
        Pose pose,
        EntityBlockTemplateMesh template,
        int cardinalLighting,
        boolean recycle
    ) {
        EntityBlockLightLayer layer = pool.pollFirst();
        if (layer == null) {
            layer = new EntityBlockLightLayer();
        }
        layer.recycle = recycle;
        layer.future = new CompletableFuture<>();
        layer.type = template.type.getLightRenderType(cardinalLighting);
        layer.template = template;
        layer.pose.set(pose.pose());
        return layer;
    }

    public static EntityBlockLightLayer resolve(
        Pose pose,
        EntityBlockTemplateMesh template,
        int cardinalLighting,
        boolean recycle
    ) {
        EntityBlockLightLayer layer = pool.pollFirst();
        if (layer == null) {
            layer = new EntityBlockLightLayer();
        }
        layer.recycle = recycle;
        layer.future = DONE;
        layer.type = template.type.getRenderType(cardinalLighting);
        layer.template = template;
        layer.pose.set(pose.pose());
        layer.positions = template.positions;
        layer.colors = template.colors;
        layer.uvs = template.uvs;
        layer.lights = template.lights;
        return layer;
    }

    public static EntityBlockLightLayer resolveLight(
        Pose pose,
        EntityBlockTemplateMesh template,
        int cardinalLighting,
        boolean recycle
    ) {
        EntityBlockLightLayer layer = pool.pollFirst();
        if (layer == null) {
            layer = new EntityBlockLightLayer();
        }
        layer.recycle = recycle;
        layer.future = DONE;
        layer.type = template.type.getLightRenderType(cardinalLighting);
        layer.template = template;
        layer.pose.set(pose.pose());
        layer.positions = template.positions;
        layer.colors = template.colors;
        layer.uvs = template.uvs;
        layer.lights = template.lights;
        return layer;
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
        if (future != null) {
            future.join();
            future = null;
            if (recycle) {
                if (index == capacity) {
                    capacity <<= 1;
                    EntityBlockLightLayer[] old = used;
                    used = new EntityBlockLightLayer[capacity];
                    System.arraycopy(old, 0, used, 0, index);
                }
                used[index++] = this;
            }
        }
        Matrix4f modelMat = pose.pose();
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
