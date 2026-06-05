package com.zurrtum.create.client.catnip.outliner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.catnip.render.BindableTexture;
import com.zurrtum.create.client.catnip.render.PonderRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector.CustomGeometryRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BlockClusterOutline extends Outline {
    private final Cluster cluster;

    protected final Vector3f pos0Temp = new Vector3f();
    protected final Vector3f pos1Temp = new Vector3f();
    protected final Vector3f pos2Temp = new Vector3f();
    protected final Vector3f pos3Temp = new Vector3f();
    protected final Vector3f normalTemp = new Vector3f();
    protected final Vector3f originTemp = new Vector3f();

    public BlockClusterOutline(Iterable<BlockPos> positions) {
        cluster = new Cluster();
        positions.forEach(cluster::include);
    }

    @Override
    public void submit(Minecraft mc, PoseStack ms, SubmitNodeCollector queue, Vec3 camera, float pt) {
        if (cluster.isEmpty()) {
            return;
        }
        int color = params.color;
        int lightmap = params.lightmap;
        boolean disableLineNormals = params.disableLineNormals;
        submitFaces(ms, queue, camera, color, lightmap);
        submitEdges(ms, queue, camera, color, lightmap, disableLineNormals);
    }

    protected void submitFaces(PoseStack ms, SubmitNodeCollector queue, Vec3 camera, int color, int lightmap) {
        BindableTexture faceTexture = params.faceTexture;
        if (faceTexture == null) {
            return;
        }
        RenderType layer = PonderRenderTypes.outlineTranslucent(faceTexture.getLocation(), true);
        ms.pushPose();
        BlockPos anchor = cluster.anchor;
        ms.translate(anchor.getX() - camera.x, anchor.getY() - camera.y, anchor.getZ() - camera.z);
        queue.submitCustomGeometry(ms, layer, new FacesRenderState(this, cluster.visibleFaces, color, lightmap));
        ms.popPose();
    }

    protected void submitEdges(
        PoseStack ms,
        SubmitNodeCollector queue,
        Vec3 camera,
        int color,
        int lightmap,
        boolean disableNormals
    ) {
        float lineWidth = params.getLineWidth();
        if (lineWidth == 0) {
            return;
        }
        ms.pushPose();
        BlockPos anchor = cluster.anchor;
        ms.translate(anchor.getX() - camera.x, anchor.getY() - camera.y, anchor.getZ() - camera.z);
        queue.submitCustomGeometry(
            ms,
            PonderRenderTypes.outlineSolid(),
            new EdgesRenderState(this, cluster.visibleEdges, lineWidth, color, lightmap, disableNormals)
        );
        ms.popPose();
    }

    public static void loadFaceData(
        Direction face,
        Vector3f pos0,
        Vector3f pos1,
        Vector3f pos2,
        Vector3f pos3,
        Vector3f normal
    ) {
        switch (face) {
            case DOWN -> {
                // 0 1 2 3
                pos0.set(0, 0, 1);
                pos1.set(0, 0, 0);
                pos2.set(1, 0, 0);
                pos3.set(1, 0, 1);
                normal.set(0, -1, 0);
            }
            case UP -> {
                // 4 5 6 7
                pos0.set(0, 1, 0);
                pos1.set(0, 1, 1);
                pos2.set(1, 1, 1);
                pos3.set(1, 1, 0);
                normal.set(0, 1, 0);
            }
            case NORTH -> {
                // 7 2 1 4
                pos0.set(1, 1, 0);
                pos1.set(1, 0, 0);
                pos2.set(0, 0, 0);
                pos3.set(0, 1, 0);
                normal.set(0, 0, -1);
            }
            case SOUTH -> {
                // 5 0 3 6
                pos0.set(0, 1, 1);
                pos1.set(0, 0, 1);
                pos2.set(1, 0, 1);
                pos3.set(1, 1, 1);
                normal.set(0, 0, 1);
            }
            case WEST -> {
                // 4 1 0 5
                pos0.set(0, 1, 0);
                pos1.set(0, 0, 0);
                pos2.set(0, 0, 1);
                pos3.set(0, 1, 1);
                normal.set(-1, 0, 0);
            }
            case EAST -> {
                // 6 3 2 7
                pos0.set(1, 1, 1);
                pos1.set(1, 0, 1);
                pos2.set(1, 0, 0);
                pos3.set(1, 1, 0);
                normal.set(1, 0, 0);
            }
        }
    }

    public static void addPos(float x, float y, float z, Vector3f pos0, Vector3f pos1, Vector3f pos2, Vector3f pos3) {
        pos0.add(x, y, z);
        pos1.add(x, y, z);
        pos2.add(x, y, z);
        pos3.add(x, y, z);
    }

    protected void bufferBlockFace(
        Pose pose,
        VertexConsumer consumer,
        BlockPos pos,
        Direction face,
        int color,
        int lightmap
    ) {
        Vector3f pos0 = pos0Temp;
        Vector3f pos1 = pos1Temp;
        Vector3f pos2 = pos2Temp;
        Vector3f pos3 = pos3Temp;
        Vector3f normal = normalTemp;

        loadFaceData(face, pos0, pos1, pos2, pos3, normal);
        addPos(
            pos.getX() + face.getStepX() / 128.0f,
            pos.getY() + face.getStepY() / 128.0f,
            pos.getZ() + face.getStepZ() / 128.0f,
            pos0,
            pos1,
            pos2,
            pos3
        );

        bufferQuad(pose, consumer, pos0, pos1, pos2, pos3, color, lightmap, normal);
    }

    private static class Cluster {

        private @Nullable BlockPos anchor;
        private final Map<MergeEntry, AxisDirection> visibleFaces;
        private final Set<MergeEntry> visibleEdges;

        public Cluster() {
            visibleEdges = new HashSet<>();
            visibleFaces = new HashMap<>();
        }

        public boolean isEmpty() {
            return anchor == null;
        }

        public void include(BlockPos pos) {
            if (anchor == null) {
                anchor = pos;
            }

            pos = pos.subtract(anchor);

            // 6 FACES
            for (Axis axis : Iterate.axes) {
                Direction direction = Direction.get(AxisDirection.POSITIVE, axis);
                for (int offset : Iterate.zeroAndOne) {
                    MergeEntry entry = new MergeEntry(axis, pos.relative(direction, offset));
                    if (visibleFaces.remove(entry) == null) {
                        visibleFaces.put(entry, offset == 0 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE);
                    }
                }
            }

            // 12 EDGES
            for (Axis axis : Iterate.axes) {
                for (Axis axis2 : Iterate.axes) {
                    if (axis == axis2) {
                        continue;
                    }
                    for (Axis axis3 : Iterate.axes) {
                        if (axis == axis3) {
                            continue;
                        }
                        if (axis2 == axis3) {
                            continue;
                        }

                        Direction direction = Direction.get(AxisDirection.POSITIVE, axis2);
                        Direction direction2 = Direction.get(AxisDirection.POSITIVE, axis3);

                        for (int offset : Iterate.zeroAndOne) {
                            BlockPos entryPos = pos.relative(direction, offset);
                            for (int offset2 : Iterate.zeroAndOne) {
                                entryPos = entryPos.relative(direction2, offset2);
                                MergeEntry entry = new MergeEntry(axis, entryPos);
                                if (!visibleEdges.remove(entry)) {
                                    visibleEdges.add(entry);
                                }
                            }
                        }
                    }

                    break;
                }
            }

        }

    }

    private static class MergeEntry {

        private final Axis axis;
        private final BlockPos pos;

        public MergeEntry(Axis axis, BlockPos pos) {
            this.axis = axis;
            this.pos = pos;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof MergeEntry other)) {
                return false;
            }

            return axis == other.axis && pos.equals(other.pos);
        }

        @Override
        public int hashCode() {
            return pos.hashCode() * 31 + axis.ordinal();
        }
    }

    protected record FacesRenderState(BlockClusterOutline outline, Map<MergeEntry, AxisDirection> visibleFaces,
                                      int color, int lightmap) implements CustomGeometryRenderer {
        @Override
        public void render(Pose pose, VertexConsumer buffer) {
            visibleFaces.forEach((face, axisDirection) -> {
                Direction direction =
                    axisDirection == AxisDirection.POSITIVE ? face.axis.getPositive() : face.axis.getNegative();
                BlockPos pos = face.pos;
                if (axisDirection == AxisDirection.POSITIVE) {
                    pos = pos.relative(direction.getOpposite());
                }
                outline.bufferBlockFace(pose, buffer, pos, direction, color, lightmap);
            });
        }
    }

    protected record EdgesRenderState(BlockClusterOutline outline, Set<MergeEntry> visibleEdges, float lineWidth,
                                      int color, int lightmap,
                                      boolean disableNormals) implements CustomGeometryRenderer {
        @Override
        public void render(Pose pose, VertexConsumer buffer) {
            Vector3f origin = outline.originTemp;
            for (MergeEntry edge : visibleEdges) {
                BlockPos pos = edge.pos;
                outline.bufferCuboidLine(
                    pose,
                    buffer,
                    origin.set(pos.getX(), pos.getY(), pos.getZ()),
                    edge.axis.getPositive(),
                    1,
                    lineWidth,
                    color,
                    lightmap,
                    disableNormals
                );
            }
        }
    }
}
