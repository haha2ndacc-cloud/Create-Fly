package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayDeque;
import java.util.Deque;

public class EntityBlockMultipleLayer implements SuperByteBufferRenderState {
    private static final Deque<EntityBlockMultipleLayer> pool = new ArrayDeque<>();
    private static int capacity = 8, index;
    private static @UnknownNullability EntityBlockMultipleLayer[] used = new EntityBlockMultipleLayer[capacity];
    private final SuperByteBufferRenderState[] layers = new SuperByteBufferRenderState[6];
    private int size;
    private boolean recycle;

    public static void recycleAll() {
        for (int i = 0; i < index; i++) {
            used[i].recycle();
        }
        index = 0;
    }

    public static void clear() {
        pool.clear();
        for (int i = 0; i < capacity; i++) {
            used[i] = null;
        }
    }

    @Override
    public void recycle() {
        for (int i = 0; i < size; i++) {
            layers[i].recycle();
        }
        size = 0;
        pool.addLast(this);
    }

    public static EntityBlockMultipleLayer create(boolean recycle) {
        EntityBlockMultipleLayer layer = pool.pollLast();
        if (layer == null) {
            layer = new EntityBlockMultipleLayer();
        }
        layer.recycle = recycle;
        return layer;
    }

    public void add(SuperByteBufferRenderState layer) {
        layers[size++] = layer;
    }

    @Override
    public void submit(PoseStack matrices, OrderedSubmitNodeCollector queue) {
        for (int i = 0; i < size; i++) {
            layers[i].submit(matrices, queue);
        }
    }

    @Override
    public void submit(Pose transform, PoseStack matrices, OrderedSubmitNodeCollector queue) {
        matrices.pushPose();
        SuperByteBuffer.mul(matrices.last(), transform);
        for (int i = 0; i < size; i++) {
            layers[i].submit(matrices, queue);
        }
        matrices.popPose();
    }

    @Override
    public void submit(RenderType type, PoseStack matrices, OrderedSubmitNodeCollector queue) {
        for (int i = 0; i < size; i++) {
            layers[i].submit(type, matrices, queue);
        }
    }

    @Override
    public void renderInto(Pose pose, VertexConsumer consumer) {
        markRecycle();
        for (int i = 0; i < size; i++) {
            layers[i].renderInto(pose, consumer);
        }
    }

    @Override
    public void render(Pose pose, VertexConsumer buffer) {
        markRecycle();
        for (int i = 0; i < size; i++) {
            layers[i].render(pose, buffer);
        }
    }

    private void markRecycle() {
        if (recycle) {
            if (index == capacity) {
                capacity <<= 1;
                EntityBlockMultipleLayer[] old = used;
                used = new EntityBlockMultipleLayer[capacity];
                System.arraycopy(old, 0, used, 0, index);
            }
            used[index++] = this;
            recycle = false;
        }
    }
}
