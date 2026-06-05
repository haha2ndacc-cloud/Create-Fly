package com.zurrtum.create.client.catnip.outliner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.catnip.render.BindableTexture;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public class AABBOutline extends Outline {
    protected AABB bb;

    protected final Vector3f pos0Temp = new Vector3f();
    protected final Vector3f pos1Temp = new Vector3f();
    protected final Vector3f pos2Temp = new Vector3f();
    protected final Vector3f pos3Temp = new Vector3f();
    protected final Vector3f normalTemp = new Vector3f();
    protected final Vector3f originTemp = new Vector3f();

    public AABBOutline(AABB bb) {
        setBounds(bb);
    }

    public AABB getBounds() {
        return bb;
    }

    public void setBounds(AABB bb) {
        this.bb = bb;
    }

    @Override
    public void submit(Minecraft mc, PoseStack ms, SubmitNodeCollector queue, Vec3 camera, float pt) {
        submitBox(ms, queue, camera, bb, params.color, params.lightmap, params.disableLineNormals);
    }

    protected void submitBox(
        PoseStack ms,
        SubmitNodeCollector queue,
        Vec3 camera,
        AABB box,
        int color,
        int lightmap,
        boolean disableLineNormals
    ) {
        Vector3f minPos = new Vector3f();
        Vector3f maxPos = new Vector3f();

        boolean cameraInside = box.contains(camera);
        boolean cull = !cameraInside && !params.disableCull;
        float inflate = cameraInside ? -1 / 128.0f : 1 / 128.0f;

        box = box.move(camera.scale(-1));
        minPos.set((float) box.minX - inflate, (float) box.minY - inflate, (float) box.minZ - inflate);
        maxPos.set((float) box.maxX + inflate, (float) box.maxY + inflate, (float) box.maxZ + inflate);

        submitBoxFaces(ms, queue, cull, params.getHighlightedFace(), minPos, maxPos, color, lightmap);
        submitBoxEdges(ms, queue, minPos, maxPos, params.getLineWidth(), color, lightmap, disableLineNormals);
    }

    protected void submitBoxFaces(
        PoseStack ms,
        SubmitNodeCollector queue,
        boolean cull,
        @Nullable Direction highlightedFace,
        Vector3f minPos,
        Vector3f maxPos,
        int color,
        int lightmap
    ) {
        BindableTexture faceTexture = params.faceTexture;
        if (faceTexture != null) {
            BoxFacesRenderState state = new BoxFacesRenderState(
                this,
                highlightedFace,
                minPos,
                maxPos,
                color,
                ARGB.multiplyAlpha(color, 0.5f),
                lightmap
            );
            RenderType layer = PonderRenderTypes.outlineTranslucent(faceTexture.getLocation(), cull);
            queue.submitCustomGeometry(ms, layer, state);
        }
    }

    protected void submitBoxEdges(
        PoseStack ms,
        SubmitNodeCollector queue,
        Vector3f minPos,
        Vector3f maxPos,
        float lineWidth,
        int color,
        int lightmap,
        boolean disableLineNormals
    ) {
        if (lineWidth != 0) {
            BoxEdgesRenderState state = new BoxEdgesRenderState(
                this,
                minPos,
                maxPos,
                lineWidth,
                color,
                lightmap,
                disableLineNormals
            );
            queue.submitCustomGeometry(ms, PonderRenderTypes.outlineSolid(), state);
        }
    }

    protected void renderBoxFace(
        Pose pose,
        VertexConsumer consumer,
        Vector3f minPos,
        Vector3f maxPos,
        Direction face,
        int color,
        int lightmap
    ) {
        Vector3f pos0 = pos0Temp;
        Vector3f pos1 = pos1Temp;
        Vector3f pos2 = pos2Temp;
        Vector3f pos3 = pos3Temp;
        Vector3f normal = normalTemp;

        float minX = minPos.x();
        float minY = minPos.y();
        float minZ = minPos.z();
        float maxX = maxPos.x();
        float maxY = maxPos.y();
        float maxZ = maxPos.z();

        float maxU;
        float maxV;

        switch (face) {
            case DOWN -> {
                // 0 1 2 3
                pos0.set(minX, minY, maxZ);
                pos1.set(minX, minY, minZ);
                pos2.set(maxX, minY, minZ);
                pos3.set(maxX, minY, maxZ);
                maxU = maxX - minX;
                maxV = maxZ - minZ;
                normal.set(0, -1, 0);
            }
            case UP -> {
                // 4 5 6 7
                pos0.set(minX, maxY, minZ);
                pos1.set(minX, maxY, maxZ);
                pos2.set(maxX, maxY, maxZ);
                pos3.set(maxX, maxY, minZ);
                maxU = maxX - minX;
                maxV = maxZ - minZ;
                normal.set(0, 1, 0);
            }
            case NORTH -> {
                // 7 2 1 4
                pos0.set(maxX, maxY, minZ);
                pos1.set(maxX, minY, minZ);
                pos2.set(minX, minY, minZ);
                pos3.set(minX, maxY, minZ);
                maxU = maxX - minX;
                maxV = maxY - minY;
                normal.set(0, 0, -1);
            }
            case SOUTH -> {
                // 5 0 3 6
                pos0.set(minX, maxY, maxZ);
                pos1.set(minX, minY, maxZ);
                pos2.set(maxX, minY, maxZ);
                pos3.set(maxX, maxY, maxZ);
                maxU = maxX - minX;
                maxV = maxY - minY;
                normal.set(0, 0, 1);
            }
            case WEST -> {
                // 4 1 0 5
                pos0.set(minX, maxY, minZ);
                pos1.set(minX, minY, minZ);
                pos2.set(minX, minY, maxZ);
                pos3.set(minX, maxY, maxZ);
                maxU = maxZ - minZ;
                maxV = maxY - minY;
                normal.set(-1, 0, 0);
            }
            case EAST -> {
                // 6 3 2 7
                pos0.set(maxX, maxY, maxZ);
                pos1.set(maxX, minY, maxZ);
                pos2.set(maxX, minY, minZ);
                pos3.set(maxX, maxY, minZ);
                maxU = maxZ - minZ;
                maxV = maxY - minY;
                normal.set(1, 0, 0);
            }
            default -> {
                maxU = 1;
                maxV = 1;
            }
        }

        bufferQuad(pose, consumer, pos0, pos1, pos2, pos3, color, 0, 0, maxU, maxV, lightmap, normal);
    }

    protected void renderBoxEdges(
        Pose pose,
        VertexConsumer consumer,
        Vector3f minPos,
        Vector3f maxPos,
        float lineWidth,
        int color,
        int lightmap,
        boolean disableNormals
    ) {
        Vector3f origin = originTemp;

        float lineLengthX = maxPos.x() - minPos.x();
        float lineLengthY = maxPos.y() - minPos.y();
        float lineLengthZ = maxPos.z() - minPos.z();

        origin.set(minPos);
        bufferCuboidLine(
            pose,
            consumer,
            origin,
            Direction.EAST,
            lineLengthX,
            lineWidth,
            color,
            lightmap,
            disableNormals
        );
        bufferCuboidLine(pose, consumer, origin, Direction.UP, lineLengthY, lineWidth, color, lightmap, disableNormals);
        bufferCuboidLine(
            pose,
            consumer,
            origin,
            Direction.SOUTH,
            lineLengthZ,
            lineWidth,
            color,
            lightmap,
            disableNormals
        );

        origin.set(maxPos.x(), minPos.y(), minPos.z());
        bufferCuboidLine(pose, consumer, origin, Direction.UP, lineLengthY, lineWidth, color, lightmap, disableNormals);
        bufferCuboidLine(
            pose,
            consumer,
            origin,
            Direction.SOUTH,
            lineLengthZ,
            lineWidth,
            color,
            lightmap,
            disableNormals
        );

        origin.set(minPos.x(), maxPos.y(), minPos.z());
        bufferCuboidLine(
            pose,
            consumer,
            origin,
            Direction.EAST,
            lineLengthX,
            lineWidth,
            color,
            lightmap,
            disableNormals
        );
        bufferCuboidLine(
            pose,
            consumer,
            origin,
            Direction.SOUTH,
            lineLengthZ,
            lineWidth,
            color,
            lightmap,
            disableNormals
        );

        origin.set(minPos.x(), minPos.y(), maxPos.z());
        bufferCuboidLine(
            pose,
            consumer,
            origin,
            Direction.EAST,
            lineLengthX,
            lineWidth,
            color,
            lightmap,
            disableNormals
        );
        bufferCuboidLine(pose, consumer, origin, Direction.UP, lineLengthY, lineWidth, color, lightmap, disableNormals);

        origin.set(minPos.x(), maxPos.y(), maxPos.z());
        bufferCuboidLine(
            pose,
            consumer,
            origin,
            Direction.EAST,
            lineLengthX,
            lineWidth,
            color,
            lightmap,
            disableNormals
        );

        origin.set(maxPos.x(), minPos.y(), maxPos.z());
        bufferCuboidLine(pose, consumer, origin, Direction.UP, lineLengthY, lineWidth, color, lightmap, disableNormals);

        origin.set(maxPos.x(), maxPos.y(), minPos.z());
        bufferCuboidLine(
            pose,
            consumer,
            origin,
            Direction.SOUTH,
            lineLengthZ,
            lineWidth,
            color,
            lightmap,
            disableNormals
        );
    }

    protected record BoxFacesRenderState(AABBOutline outline, @Nullable Direction highlightedFace, Vector3f minPos,
                                         Vector3f maxPos, int highlightColor, int alphaColor,
                                         int lightmap) implements SubmitNodeCollector.CustomGeometryRenderer {
        @Override
        public void render(Pose pose, VertexConsumer buffer) {
            renderBoxFace(pose, buffer, Direction.DOWN);
            renderBoxFace(pose, buffer, Direction.UP);
            renderBoxFace(pose, buffer, Direction.NORTH);
            renderBoxFace(pose, buffer, Direction.SOUTH);
            renderBoxFace(pose, buffer, Direction.WEST);
            renderBoxFace(pose, buffer, Direction.EAST);
        }

        protected void renderBoxFace(Pose pose, VertexConsumer buffer, Direction face) {
            int color = face == highlightedFace ? highlightColor : alphaColor;
            outline.renderBoxFace(pose, buffer, minPos, maxPos, face, color, lightmap);
        }
    }

    protected record BoxEdgesRenderState(AABBOutline outline, Vector3f minPos, Vector3f maxPos, float lineWidth,
                                         int color, int lightmap,
                                         boolean disableLineNormals) implements SubmitNodeCollector.CustomGeometryRenderer {
        @Override
        public void render(Pose pose, VertexConsumer buffer) {
            outline.renderBoxEdges(pose, buffer, minPos, maxPos, lineWidth, color, lightmap, disableLineNormals);
        }
    }
}
