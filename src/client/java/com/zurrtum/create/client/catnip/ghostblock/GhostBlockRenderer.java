package com.zurrtum.create.client.catnip.ghostblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.catnip.placement.PlacementClient;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import com.zurrtum.create.client.flywheel.lib.model.baked.EmptyVirtualBlockGetter;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

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
        ModelBlockRenderer blockRenderer,
        BlockStateModelSet blockStateModelSet,
        PoseStack ms,
        SuperRenderTypeBuffer buffer,
        Vec3 camera,
        GhostBlockParams params
    );

    private static class DefaultGhostBlockRenderer extends GhostBlockRenderer {
        private static final RenderType[] RENDER_TYPES = new RenderType[]{RenderTypes.solidMovingBlock(), RenderTypes.cutoutMovingBlock(), RenderTypes.translucentMovingBlock()};

        @Override
        public void render(
            ModelBlockRenderer blockRenderer,
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
            Matrix4f pose = entry.pose();
            Matrix4f origin = new Matrix4f(pose);
            blockRenderer.tesselateBlock(
                (x, y, z, quad, instance) -> {
                    pose.set(origin);
                    pose.translate(x, y, z);
                    buffer.getEarlyBuffer(RENDER_TYPES[quad.materialInfo().layer().ordinal()])
                        .putBakedQuad(entry, quad, instance);
                },
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
    }

    private static class TransparentGhostBlockRenderer extends GhostBlockRenderer {
        @Override
        public void render(
            ModelBlockRenderer blockRenderer,
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
            Matrix4f pose = entry.pose();
            Matrix4f origin = new Matrix4f(pose);
            VertexConsumer consumer = buffer.getEarlyBuffer(RenderTypes.translucentMovingBlock());
            float alpha = params.alphaSupplier.get() * 0.75f * PlacementClient.getCurrentAlpha();
            blockRenderer.tesselateBlock(
                (x, y, z, quad, instance) -> {
                    pose.set(origin);
                    pose.translate(x, y, z);
                    instance.setColor(0, ARGB.multiplyAlpha(instance.getColor(0), alpha));
                    instance.setColor(1, ARGB.multiplyAlpha(instance.getColor(1), alpha));
                    instance.setColor(2, ARGB.multiplyAlpha(instance.getColor(2), alpha));
                    instance.setColor(3, ARGB.multiplyAlpha(instance.getColor(3), alpha));
                    consumer.putBakedQuad(entry, quad, instance);
                },
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
    }
}
