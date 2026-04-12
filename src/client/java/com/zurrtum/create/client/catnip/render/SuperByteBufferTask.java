package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.BlockAndLightGetter;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedTransferQueue;

public class SuperByteBufferTask {
    private static final Pose IDENTITY_ENTRY = new Pose();
    private static final Matrix4fc IDENTITY_POSE = IDENTITY_ENTRY.pose();
    public Pose pose = new Pose();
    public @UnknownNullability AbstractEntityBlockLayer layer;
    public @UnknownNullability EntityBlockTemplateMesh template;
    public byte flag;
    public int color;
    public @UnknownNullability SpriteShiftEntry shiftEntry;
    float shiftU;
    float shiftV;
    int sheetSize;
    int packedLight;
    public @UnknownNullability BlockAndLightGetter blockAndLightGetter;
    public @Nullable Matrix4fc lightTransform;
    int overlay = OverlayTexture.NO_OVERLAY;

    public Matrix4fc copyPose() {
        Matrix4f mat = pose.pose();
        return (mat.properties() & Matrix4fc.PROPERTY_IDENTITY) != 0 ? IDENTITY_POSE : new Matrix4f(mat);
    }

    public Pose copyEntry() {
        Matrix4f mat = pose.pose();
        return (mat.properties() & Matrix4fc.PROPERTY_IDENTITY) != 0 ? IDENTITY_ENTRY : pose.copy();
    }

    public SuperByteBufferRenderState resolve(EntityBlockTemplateMesh[] templates) {
        int size = templates.length;
        if (size == 1) {
            SuperByteBufferRenderState state;
            if ((flag & 0b100000) != 0) {
                state = EntityBlockLightLayer.light(copyPose(), templates[0], flag >>> 6);
            } else {
                EntityBlockTemplateMesh template = templates[0];
                if (template.type.isLight()) {
                    state = EntityBlockLightLayer.create(copyPose(), template, flag >>> 6);
                } else {
                    state = EntityBlockLayer.create(copyEntry(), template, overlay, flag >>> 6);
                }
            }
            reset();
            return state;
        }
        int cardinalLighting = flag >>> 6;
        SuperByteBufferRenderState[] states = new SuperByteBufferRenderState[size];
        if ((flag & 0b100000) != 0) {
            Matrix4fc pose = copyPose();
            for (int i = 0; i < size; i++) {
                states[i] = EntityBlockLightLayer.light(pose, templates[i], cardinalLighting);
            }
        } else {
            Pose pose = copyEntry();
            for (int i = 0; i < size; i++) {
                EntityBlockTemplateMesh template = templates[i];
                if (template.type.isLight()) {
                    states[i] = EntityBlockLightLayer.create(pose.pose(), template, cardinalLighting);
                } else {
                    states[i] = EntityBlockLayer.create(pose, template, overlay, cardinalLighting);
                }
            }
        }
        reset();
        return new EntityBlockMultipleLayer(states);
    }

    private void submit(
        Matrix4fc pose,
        LinkedTransferQueue<SuperByteBufferTask> queue,
        EntityBlockTemplateMesh[] templates,
        SuperByteBufferRenderState[] states,
        int cardinalLighting,
        int i
    ) {
        template = templates[i];
        states[i] = layer = new EntityBlockLightLayer(pose, template.type.getLightRenderType(cardinalLighting));
        queue.put(this);
    }

    private void submit(
        Pose pose,
        int overlay,
        LinkedTransferQueue<SuperByteBufferTask> queue,
        EntityBlockTemplateMesh[] templates,
        SuperByteBufferRenderState[] states,
        int cardinalLighting,
        int i
    ) {
        template = templates[i];
        RenderType type = template.type.getRenderType(cardinalLighting);
        if (template.type.isLight()) {
            states[i] = layer = new EntityBlockLightLayer(pose.pose(), type);
        } else {
            states[i] = layer = new EntityBlockLayer(pose, type, template.normals, overlay);
        }
        queue.put(this);
    }

    public SuperByteBufferRenderState submit(
        LinkedTransferQueue<SuperByteBufferTask> queue,
        ConcurrentLinkedQueue<SuperByteBufferTask> pool,
        EntityBlockTemplateMesh[] templates
    ) {
        int size = templates.length;
        if (size == 1) {
            template = templates[0];
            SuperByteBufferRenderState state;
            if ((flag & 0b100000) != 0) {
                state = layer = new EntityBlockLightLayer(copyPose(), template.type.getLightRenderType(flag >>> 6));
            } else {
                RenderType type = template.type.getRenderType(flag >>> 6);
                if (template.type.isLight()) {
                    state = layer = new EntityBlockLightLayer(copyPose(), type);
                } else {
                    state = layer = new EntityBlockLayer(copyEntry(), type, template.normals, overlay);
                }
            }
            queue.put(this);
            return state;
        }
        int cardinalLighting = flag >>> 6;
        SuperByteBufferRenderState[] states = new SuperByteBufferRenderState[size];
        int end = size - 1;
        if ((flag & 0b100000) != 0) {
            Matrix4fc pose = copyPose();
            for (int i = 0; i < end; i++) {
                SuperByteBufferTask task = pool.poll();
                if (task == null) {
                    task = new SuperByteBufferTask();
                }
                task.set(this);
                task.submit(pose, queue, templates, states, cardinalLighting, i);
            }
            submit(pose, queue, templates, states, cardinalLighting, end);
        } else {
            Pose pose = copyEntry();
            for (int i = 0; i < end; i++) {
                SuperByteBufferTask task = pool.poll();
                if (task == null) {
                    task = new SuperByteBufferTask();
                }
                task.set(this);
                task.submit(pose, overlay, queue, templates, states, cardinalLighting, i);
            }
            submit(pose, overlay, queue, templates, states, cardinalLighting, end);
        }
        return new EntityBlockMultipleLayer(states);
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
        template = null;
        flag = 0;
        shiftEntry = null;
        blockAndLightGetter = null;
        lightTransform = null;
        overlay = OverlayTexture.NO_OVERLAY;
    }
}
