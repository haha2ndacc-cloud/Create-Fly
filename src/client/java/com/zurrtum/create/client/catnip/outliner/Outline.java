package com.zurrtum.create.client.catnip.outliner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.render.BindableTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

public abstract class Outline {
    protected final OutlineParams params;

    protected final Vector3f minPosTemp = new Vector3f();
    protected final Vector3f maxPosTemp = new Vector3f();
    protected final Vector4f posTransformTemp = new Vector4f();
    protected final Vector3f normalTransformTemp = new Vector3f();

    public Outline() {
        params = new OutlineParams();
    }

    public OutlineParams getParams() {
        return params;
    }

    public abstract void submit(Minecraft mc, PoseStack ms, SubmitNodeCollector queue, Vec3 camera, float pt);

    public void tick() {
    }

    public void bufferCuboidLine(
        Pose pose,
        VertexConsumer consumer,
        Vector3f origin,
        Direction direction,
        float length,
        float width,
        int color,
        int lightmap,
        boolean disableNormals
    ) {
        Vector3f minPos = minPosTemp;
        Vector3f maxPos = maxPosTemp;

        float halfWidth = width / 2;
        minPos.set(origin.x() - halfWidth, origin.y() - halfWidth, origin.z() - halfWidth);
        maxPos.set(origin.x() + halfWidth, origin.y() + halfWidth, origin.z() + halfWidth);

        switch (direction) {
            case DOWN -> {
                minPos.add(0, -length, 0);
            }
            case UP -> {
                maxPos.add(0, length, 0);
            }
            case NORTH -> {
                minPos.add(0, 0, -length);
            }
            case SOUTH -> {
                maxPos.add(0, 0, length);
            }
            case WEST -> {
                minPos.add(-length, 0, 0);
            }
            case EAST -> {
                maxPos.add(length, 0, 0);
            }
        }

        bufferCuboid(pose, consumer, minPos, maxPos, color, lightmap, disableNormals);
    }

    public void bufferCuboid(
        Pose pose,
        VertexConsumer consumer,
        Vector3f minPos,
        Vector3f maxPos,
        int color,
        int lightmap,
        boolean disableNormals
    ) {
        Vector4f posTransformTemp = this.posTransformTemp;
        Vector3f normalTransformTemp = this.normalTransformTemp;

        float minX = minPos.x();
        float minY = minPos.y();
        float minZ = minPos.z();
        float maxX = maxPos.x();
        float maxY = maxPos.y();
        float maxZ = maxPos.z();

        Matrix4f posMatrix = pose.pose();

        posTransformTemp.set(minX, minY, maxZ, 1);
        posTransformTemp.mul(posMatrix);
        float x0 = posTransformTemp.x();
        float y0 = posTransformTemp.y();
        float z0 = posTransformTemp.z();

        posTransformTemp.set(minX, minY, minZ, 1);
        posTransformTemp.mul(posMatrix);
        float x1 = posTransformTemp.x();
        float y1 = posTransformTemp.y();
        float z1 = posTransformTemp.z();

        posTransformTemp.set(maxX, minY, minZ, 1);
        posTransformTemp.mul(posMatrix);
        float x2 = posTransformTemp.x();
        float y2 = posTransformTemp.y();
        float z2 = posTransformTemp.z();

        posTransformTemp.set(maxX, minY, maxZ, 1);
        posTransformTemp.mul(posMatrix);
        float x3 = posTransformTemp.x();
        float y3 = posTransformTemp.y();
        float z3 = posTransformTemp.z();

        posTransformTemp.set(minX, maxY, minZ, 1);
        posTransformTemp.mul(posMatrix);
        float x4 = posTransformTemp.x();
        float y4 = posTransformTemp.y();
        float z4 = posTransformTemp.z();

        posTransformTemp.set(minX, maxY, maxZ, 1);
        posTransformTemp.mul(posMatrix);
        float x5 = posTransformTemp.x();
        float y5 = posTransformTemp.y();
        float z5 = posTransformTemp.z();

        posTransformTemp.set(maxX, maxY, maxZ, 1);
        posTransformTemp.mul(posMatrix);
        float x6 = posTransformTemp.x();
        float y6 = posTransformTemp.y();
        float z6 = posTransformTemp.z();

        posTransformTemp.set(maxX, maxY, minZ, 1);
        posTransformTemp.mul(posMatrix);
        float x7 = posTransformTemp.x();
        float y7 = posTransformTemp.y();
        float z7 = posTransformTemp.z();

        Matrix3f normalMatrix = pose.normal();

        // down

        if (disableNormals) {
            normalTransformTemp.set(0, 1, 0);
        } else {
            normalTransformTemp.set(0, -1, 0);
        }
        normalTransformTemp.mul(normalMatrix);
        float nx0 = normalTransformTemp.x();
        float ny0 = normalTransformTemp.y();
        float nz0 = normalTransformTemp.z();

        consumer.addVertex(x0, y0, z0, color, 0, 0, OverlayTexture.NO_OVERLAY, lightmap, nx0, ny0, nz0);
        consumer.addVertex(x1, y1, z1, color, 0, 1, OverlayTexture.NO_OVERLAY, lightmap, nx0, ny0, nz0);
        consumer.addVertex(x2, y2, z2, color, 1, 1, OverlayTexture.NO_OVERLAY, lightmap, nx0, ny0, nz0);
        consumer.addVertex(x3, y3, z3, color, 1, 0, OverlayTexture.NO_OVERLAY, lightmap, nx0, ny0, nz0);

        // up

        normalTransformTemp.set(0, 1, 0);
        normalTransformTemp.mul(normalMatrix);
        float nx1 = normalTransformTemp.x();
        float ny1 = normalTransformTemp.y();
        float nz1 = normalTransformTemp.z();

        consumer.addVertex(x4, y4, z4, color, 0, 0, OverlayTexture.NO_OVERLAY, lightmap, nx1, ny1, nz1);
        consumer.addVertex(x5, y5, z5, color, 0, 1, OverlayTexture.NO_OVERLAY, lightmap, nx1, ny1, nz1);
        consumer.addVertex(x6, y6, z6, color, 1, 1, OverlayTexture.NO_OVERLAY, lightmap, nx1, ny1, nz1);
        consumer.addVertex(x7, y7, z7, color, 1, 0, OverlayTexture.NO_OVERLAY, lightmap, nx1, ny1, nz1);

        // north

        if (disableNormals) {
            normalTransformTemp.set(0, 1, 0);
        } else {
            normalTransformTemp.set(0, 0, -1);
        }
        normalTransformTemp.mul(normalMatrix);
        float nx2 = normalTransformTemp.x();
        float ny2 = normalTransformTemp.y();
        float nz2 = normalTransformTemp.z();

        consumer.addVertex(x7, y7, z7, color, 0, 0, OverlayTexture.NO_OVERLAY, lightmap, nx2, ny2, nz2);
        consumer.addVertex(x2, y2, z2, color, 0, 1, OverlayTexture.NO_OVERLAY, lightmap, nx2, ny2, nz2);
        consumer.addVertex(x1, y1, z1, color, 1, 1, OverlayTexture.NO_OVERLAY, lightmap, nx2, ny2, nz2);
        consumer.addVertex(x4, y4, z4, color, 1, 0, OverlayTexture.NO_OVERLAY, lightmap, nx2, ny2, nz2);

        // south

        if (disableNormals) {
            normalTransformTemp.set(0, 1, 0);
        } else {
            normalTransformTemp.set(0, 0, 1);
        }
        normalTransformTemp.mul(normalMatrix);
        float nx3 = normalTransformTemp.x();
        float ny3 = normalTransformTemp.y();
        float nz3 = normalTransformTemp.z();

        consumer.addVertex(x5, y5, z5, color, 0, 0, OverlayTexture.NO_OVERLAY, lightmap, nx3, ny3, nz3);
        consumer.addVertex(x0, y0, z0, color, 0, 1, OverlayTexture.NO_OVERLAY, lightmap, nx3, ny3, nz3);
        consumer.addVertex(x3, y3, z3, color, 1, 1, OverlayTexture.NO_OVERLAY, lightmap, nx3, ny3, nz3);
        consumer.addVertex(x6, y6, z6, color, 1, 0, OverlayTexture.NO_OVERLAY, lightmap, nx3, ny3, nz3);

        // west

        if (disableNormals) {
            normalTransformTemp.set(0, 1, 0);
        } else {
            normalTransformTemp.set(-1, 0, 0);
        }
        normalTransformTemp.mul(normalMatrix);
        float nx4 = normalTransformTemp.x();
        float ny4 = normalTransformTemp.y();
        float nz4 = normalTransformTemp.z();

        consumer.addVertex(x4, y4, z4, color, 0, 0, OverlayTexture.NO_OVERLAY, lightmap, nx4, ny4, nz4);
        consumer.addVertex(x1, y1, z1, color, 0, 1, OverlayTexture.NO_OVERLAY, lightmap, nx4, ny4, nz4);
        consumer.addVertex(x0, y0, z0, color, 1, 1, OverlayTexture.NO_OVERLAY, lightmap, nx4, ny4, nz4);
        consumer.addVertex(x5, y5, z5, color, 1, 0, OverlayTexture.NO_OVERLAY, lightmap, nx4, ny4, nz4);

        // east

        if (disableNormals) {
            normalTransformTemp.set(0, 1, 0);
        } else {
            normalTransformTemp.set(1, 0, 0);
        }
        normalTransformTemp.mul(normalMatrix);
        float nx5 = normalTransformTemp.x();
        float ny5 = normalTransformTemp.y();
        float nz5 = normalTransformTemp.z();

        consumer.addVertex(x6, y6, z6, color, 0, 0, OverlayTexture.NO_OVERLAY, lightmap, nx5, ny5, nz5);
        consumer.addVertex(x3, y3, z3, color, 0, 1, OverlayTexture.NO_OVERLAY, lightmap, nx5, ny5, nz5);
        consumer.addVertex(x2, y2, z2, color, 1, 1, OverlayTexture.NO_OVERLAY, lightmap, nx5, ny5, nz5);
        consumer.addVertex(x7, y7, z7, color, 1, 0, OverlayTexture.NO_OVERLAY, lightmap, nx5, ny5, nz5);
    }

    public void bufferQuad(
        Pose pose,
        VertexConsumer consumer,
        Vector3f pos0,
        Vector3f pos1,
        Vector3f pos2,
        Vector3f pos3,
        int color,
        int lightmap,
        Vector3f normal
    ) {
        bufferQuad(pose, consumer, pos0, pos1, pos2, pos3, color, 0, 0, 1, 1, lightmap, normal);
    }

    public void bufferQuad(
        Pose pose,
        VertexConsumer consumer,
        Vector3f pos0,
        Vector3f pos1,
        Vector3f pos2,
        Vector3f pos3,
        int color,
        float minU,
        float minV,
        float maxU,
        float maxV,
        int lightmap,
        Vector3f normal
    ) {
        Vector4f posTransformTemp = this.posTransformTemp;
        Vector3f normalTransformTemp = this.normalTransformTemp;

        Matrix4f posMatrix = pose.pose();

        posTransformTemp.set(pos0.x(), pos0.y(), pos0.z(), 1);
        posTransformTemp.mul(posMatrix);
        float x0 = posTransformTemp.x();
        float y0 = posTransformTemp.y();
        float z0 = posTransformTemp.z();

        posTransformTemp.set(pos1.x(), pos1.y(), pos1.z(), 1);
        posTransformTemp.mul(posMatrix);
        float x1 = posTransformTemp.x();
        float y1 = posTransformTemp.y();
        float z1 = posTransformTemp.z();

        posTransformTemp.set(pos2.x(), pos2.y(), pos2.z(), 1);
        posTransformTemp.mul(posMatrix);
        float x2 = posTransformTemp.x();
        float y2 = posTransformTemp.y();
        float z2 = posTransformTemp.z();

        posTransformTemp.set(pos3.x(), pos3.y(), pos3.z(), 1);
        posTransformTemp.mul(posMatrix);
        float x3 = posTransformTemp.x();
        float y3 = posTransformTemp.y();
        float z3 = posTransformTemp.z();

        normalTransformTemp.set(normal);
        normalTransformTemp.mul(pose.normal());
        float nx = normalTransformTemp.x();
        float ny = normalTransformTemp.y();
        float nz = normalTransformTemp.z();

        consumer.addVertex(x0, y0, z0, color, minU, minV, OverlayTexture.NO_OVERLAY, lightmap, nx, ny, nz);
        consumer.addVertex(x1, y1, z1, color, minU, maxV, OverlayTexture.NO_OVERLAY, lightmap, nx, ny, nz);
        consumer.addVertex(x2, y2, z2, color, maxU, maxV, OverlayTexture.NO_OVERLAY, lightmap, nx, ny, nz);
        consumer.addVertex(x3, y3, z3, color, maxU, minV, OverlayTexture.NO_OVERLAY, lightmap, nx, ny, nz);
    }

    public static class OutlineParams {
        @Nullable
        protected BindableTexture faceTexture;
        @Nullable
        protected BindableTexture highlightedFaceTexture;
        @Nullable Direction highlightedFace;
        protected boolean fadeLineWidth;
        protected boolean disableCull;
        protected boolean disableLineNormals;
        protected float alpha;
        protected int lightmap;
        protected int color;
        private float lineWidth;

        public OutlineParams() {
            faceTexture = highlightedFaceTexture = null;
            alpha = 1;
            lineWidth = 1 / 32.0f;
            fadeLineWidth = true;
            color = 0xFFFFFFFF;
            lightmap = LightCoordsUtil.FULL_BRIGHT;
        }

        // builder

        public OutlineParams colored(int color) {
            this.color = color | 0xFF000000;
            return this;
        }

        public OutlineParams colored(Color c) {
            color = c.getRGB();
            return this;
        }

        public OutlineParams lightmap(int light) {
            lightmap = light;
            return this;
        }

        public OutlineParams lineWidth(float width) {
            lineWidth = width;
            return this;
        }

        public OutlineParams withFaceTexture(@Nullable BindableTexture texture) {
            faceTexture = texture;
            return this;
        }

        public OutlineParams clearTextures() {
            return withFaceTextures(null, null);
        }

        public OutlineParams withFaceTextures(
            @Nullable BindableTexture texture,
            @Nullable BindableTexture highlightTexture
        ) {
            faceTexture = texture;
            highlightedFaceTexture = highlightTexture;
            return this;
        }

        public OutlineParams highlightFace(@Nullable Direction face) {
            highlightedFace = face;
            return this;
        }

        public OutlineParams disableLineNormals() {
            disableLineNormals = true;
            return this;
        }

        public OutlineParams disableCull() {
            disableCull = true;
            return this;
        }

        // getter

        public float getLineWidth() {
            return fadeLineWidth ? alpha * lineWidth : lineWidth;
        }

        @Nullable
        public Direction getHighlightedFace() {
            return highlightedFace;
        }

        public int getColor() {
            return color;
        }
    }
}
