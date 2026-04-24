package com.zurrtum.create.client.catnip.ghostblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.catnip.placement.PlacementClient;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import com.zurrtum.create.client.flywheel.lib.model.baked.BufferColorPoseEmitter;
import com.zurrtum.create.client.flywheel.lib.model.baked.BufferPoseEmitter;
import com.zurrtum.create.client.flywheel.lib.model.baked.EmptyVirtualBlockGetter;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelRenderHelper;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;

public abstract class GhostBlockRenderer {

    private static final GhostBlockRenderer STANDARD = new DefaultGhostBlockRenderer();
    private static final GhostBlockRenderer TRANSPARENT = new TransparentGhostBlockRenderer();

    public static GhostBlockRenderer standard() {
        return STANDARD;
    }

    public static GhostBlockRenderer transparent() {
        return TRANSPARENT;
    }

    public abstract void render(
        BlockStateModelSet blockStateModelSet,
        PoseStack ms,
        SuperRenderTypeBuffer buffer,
        Vec3 camera,
        GhostBlockParams params
    );

    private static class DefaultGhostBlockRenderer extends GhostBlockRenderer {
        private static final Output output = new Output();

        @Override
        public void render(
            BlockStateModelSet blockStateModelSet,
            PoseStack ms,
            SuperRenderTypeBuffer buffer,
            Vec3 camera,
            GhostBlockParams params
        ) {
            BlockState state = params.state;
            BlockPos pos = params.pos;
            ms.pushPose();
            ms.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
            output.update(buffer, ms);
            ModelRenderHelper.getHelper(output).tesselateBlock(
                0,
                0,
                0,
                EmptyVirtualBlockGetter.FULL_BRIGHT,
                pos,
                state,
                blockStateModelSet.get(state),
                state.getSeed(pos)
            );
            ms.popPose();
        }

        private static class Output implements BufferPoseEmitter {
            private @UnknownNullability SuperRenderTypeBuffer buffer;
            private @UnknownNullability Pose origin;
            private final Pose pose = new Pose();

            public void update(SuperRenderTypeBuffer buffer, PoseStack ms) {
                this.buffer = buffer;
                origin = ms.last();
            }

            @Override
            public Pose getPose() {
                return origin;
            }

            @Override
            public VertexConsumer getBuffer(boolean shade, ChunkSectionLayer layer) {
                return buffer.getEarlyBuffer(switch (layer) {
                    case SOLID -> RenderTypes.solidMovingBlock();
                    case CUTOUT -> RenderTypes.cutoutMovingBlock();
                    case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
                });
            }

            @Override
            public void put(float x, float y, float z, BakedQuad quad, QuadInstance instance) {
                VertexConsumer buffer = getBuffer(true, quad.materialInfo().layer());
                pose.set(origin);
                pose.translate(x, y, z);
                buffer.putBakedQuad(pose, quad, instance);
            }
        }
    }

    private static class TransparentGhostBlockRenderer extends GhostBlockRenderer {
        private static final Output output = new Output();

        @Override
        public void render(
            BlockStateModelSet blockStateModelSet,
            PoseStack ms,
            SuperRenderTypeBuffer buffer,
            Vec3 camera,
            GhostBlockParams params
        ) {
            BlockState state = params.state;
            BlockPos pos = params.pos;
            ms.pushPose();
            ms.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
            Pose entry = ms.last();
            SuperByteBuffer.scaleAround(entry, 0.85f, 0.5f, 0.5f, 0.5f);
            output.update(buffer, ms, params);
            ModelRenderHelper.getHelper(output).tesselateBlock(
                0,
                0,
                0,
                EmptyVirtualBlockGetter.FULL_BRIGHT,
                pos,
                state,
                blockStateModelSet.get(state),
                state.getSeed(pos)
            );
            ms.popPose();
        }

        private static class Output implements BufferColorPoseEmitter {
            private @UnknownNullability Pose origin;
            private final Pose pose = new Pose();
            private @UnknownNullability VertexConsumer buffer;
            private float alpha;

            public void update(SuperRenderTypeBuffer buffer, PoseStack ms, GhostBlockParams params) {
                this.buffer = buffer.getEarlyBuffer(RenderTypes.translucentMovingBlock());
                origin = ms.last();
                alpha = params.alphaSupplier.get() * 0.75f * PlacementClient.getCurrentAlpha();
            }

            @Override
            public int getColor() {
                return ARGB.white(alpha);
            }

            @Override
            public Pose getPose() {
                return origin;
            }

            @Override
            public VertexConsumer getBuffer(boolean shade, ChunkSectionLayer layer) {
                return buffer;
            }

            @Override
            public void put(float x, float y, float z, BakedQuad quad, QuadInstance instance) {
                pose.set(origin);
                pose.translate(x, y, z);
                instance.setColor(0, ARGB.multiplyAlpha(instance.getColor(0), alpha));
                instance.setColor(1, ARGB.multiplyAlpha(instance.getColor(1), alpha));
                instance.setColor(2, ARGB.multiplyAlpha(instance.getColor(2), alpha));
                instance.setColor(3, ARGB.multiplyAlpha(instance.getColor(3), alpha));
                buffer.putBakedQuad(pose, quad, instance);
            }
        }
    }
}
