package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.BlockAndLightGetter;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedTransferQueue;

public class SuperByteBufferTask {
    public final Pose pose = new Pose();
    public @UnknownNullability AbstractEntityBlockLayer layer;
    public int flag;
    public int color;
    public @UnknownNullability SpriteShiftEntry shiftEntry;
    float shiftU;
    float shiftV;
    int sheetSize;
    int packedLight;
    public @UnknownNullability BlockAndLightGetter blockAndLightGetter;
    public @Nullable Matrix4fc lightTransform;
    int overlay = OverlayTexture.NO_OVERLAY;

    public SuperByteBufferRenderState resolve(EntityBlockTemplateMesh[] templates) {
        int size = templates.length;
        if (size == 1) {
            SuperByteBufferRenderState state;
            if ((flag & 0b100000) != 0) {
                state = EntityBlockLightLayer.resolveLight(pose, templates[0], flag >>> 7, (flag & 0b1000000) == 0);
            } else {
                EntityBlockTemplateMesh template = templates[0];
                if (template.type.isLight()) {
                    state = EntityBlockLightLayer.resolve(pose, template, flag >>> 7, (flag & 0b1000000) == 0);
                } else {
                    state = EntityBlockLayer.resolve(pose, template, overlay, flag >>> 7, (flag & 0b1000000) == 0);
                }
            }
            reset();
            return state;
        }
        boolean recycle = (flag & 0b1000000) == 0;
        int cardinalLighting = flag >>> 7;
        EntityBlockMultipleLayer layers = EntityBlockMultipleLayer.create(recycle);
        if ((flag & 0b100000) != 0) {
            for (int i = 0; i < size; i++) {
                layers.add(EntityBlockLightLayer.resolveLight(pose, templates[i], cardinalLighting, recycle));
            }
        } else {
            for (int i = 0; i < size; i++) {
                EntityBlockTemplateMesh template = templates[i];
                if (template.type.isLight()) {
                    layers.add(EntityBlockLightLayer.resolve(pose, template, cardinalLighting, recycle));
                } else {
                    layers.add(EntityBlockLayer.resolve(pose, template, overlay, cardinalLighting, recycle));
                }
            }
        }
        reset();
        return layers;
    }

    private void submit(
        Pose pose,
        LinkedTransferQueue<SuperByteBufferTask> queue,
        EntityBlockTemplateMesh[] templates,
        EntityBlockMultipleLayer layers,
        boolean recycle,
        int cardinalLighting,
        int i
    ) {
        layer = EntityBlockLightLayer.createLight(pose, templates[i], cardinalLighting, recycle);
        layers.add(layer);
        queue.put(this);
    }

    private void submit(
        Pose pose,
        int overlay,
        LinkedTransferQueue<SuperByteBufferTask> queue,
        EntityBlockTemplateMesh[] templates,
        EntityBlockMultipleLayer layers,
        boolean recycle,
        int cardinalLighting,
        int i
    ) {
        EntityBlockTemplateMesh template = templates[i];
        if (template.type.isLight()) {
            layer = EntityBlockLightLayer.create(pose, template, cardinalLighting, recycle);
        } else {
            layer = EntityBlockLayer.create(pose, template, overlay, cardinalLighting, recycle);
        }
        layers.add(layer);
        queue.put(this);
    }

    public SuperByteBufferRenderState submit(
        LinkedTransferQueue<SuperByteBufferTask> queue,
        ConcurrentLinkedQueue<SuperByteBufferTask> pool,
        EntityBlockTemplateMesh[] templates
    ) {
        int size = templates.length;
        if (size == 1) {
            EntityBlockTemplateMesh template = templates[0];
            SuperByteBufferRenderState state;
            if ((flag & 0b100000) != 0) {
                state = layer = EntityBlockLightLayer.createLight(pose, template, flag >>> 7, (flag & 0b1000000) == 0);
            } else {
                if (template.type.isLight()) {
                    state = layer = EntityBlockLightLayer.create(pose, template, flag >>> 7, (flag & 0b1000000) == 0);
                } else {
                    state = layer = EntityBlockLayer.create(
                        pose,
                        template,
                        overlay,
                        flag >>> 7,
                        (flag & 0b1000000) == 0
                    );
                }
            }
            queue.put(this);
            return state;
        }
        boolean recycle = (flag & 0b1000000) == 0;
        int cardinalLighting = flag >>> 7;
        EntityBlockMultipleLayer layers = EntityBlockMultipleLayer.create(recycle);
        int end = size - 1;
        if ((flag & 0b100000) != 0) {
            for (int i = 0; i < end; i++) {
                SuperByteBufferTask task = pool.poll();
                if (task == null) {
                    task = new SuperByteBufferTask();
                }
                task.set(this);
                task.submit(pose, queue, templates, layers, recycle, cardinalLighting, i);
            }
            submit(pose, queue, templates, layers, recycle, cardinalLighting, end);
        } else {
            for (int i = 0; i < end; i++) {
                SuperByteBufferTask task = pool.poll();
                if (task == null) {
                    task = new SuperByteBufferTask();
                }
                task.set(this);
                task.submit(pose, overlay, queue, templates, layers, recycle, cardinalLighting, i);
            }
            submit(pose, overlay, queue, templates, layers, recycle, cardinalLighting, end);
        }
        return layers;
    }

    public void set(SuperByteBufferTask origin) {
        flag = origin.flag;
        color = origin.color;
        shiftEntry = origin.shiftEntry;
        shiftU = origin.shiftU;
        shiftV = origin.shiftV;
        sheetSize = origin.sheetSize;
        packedLight = origin.packedLight;
        blockAndLightGetter = origin.blockAndLightGetter;
        lightTransform = origin.lightTransform;
    }

    public void reset() {
        pose.setIdentity();
        layer = null;
        flag = 0;
        shiftEntry = null;
        blockAndLightGetter = null;
        lightTransform = null;
        overlay = OverlayTexture.NO_OVERLAY;
    }
}
