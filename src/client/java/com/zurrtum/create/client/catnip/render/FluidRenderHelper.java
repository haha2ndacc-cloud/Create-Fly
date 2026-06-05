package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllFluidConfigs;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

public class FluidRenderHelper {
    public static FluidRenderState extractFluidRenderState(
        @Nullable BlockAndTintGetter level,
        @Nullable BlockPos pos,
        FluidStateModelSet fluidStateModelSet,
        Fluid fluid,
        DataComponentPatch components,
        float xMin,
        float yMin,
        float zMin,
        float xMax,
        float yMax,
        float zMax,
        int lightCoords,
        boolean renderBottom,
        boolean invertGasses
    ) {
        FluidState fluidState = fluid.defaultFluidState();
        BlockState blockState = fluidState.createLegacyBlock();
        FluidModel model = fluidStateModelSet.get(fluidState);
        RenderType layer = switch (model.layer()) {
            case SOLID -> RenderTypes.solidMovingBlock();
            case CUTOUT -> RenderTypes.cutoutMovingBlock();
            case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
        };
        TextureAtlasSprite fluidTexture = model.stillMaterial().sprite();
        QuadRenderHelper.markSpriteActive(fluidTexture);
        return new FluidRenderState(
            layer,
            fluidTexture,
            AllFluidConfigs.getTint(level, pos, blockState, model, fluid, components) | 0xff000000,
            blockState.getLightEmission(),
            xMin,
            yMin,
            zMin,
            xMax,
            yMax,
            zMax,
            lightCoords,
            renderBottom,
            invertGasses
        );
    }

    public static void renderFluidBox(
        TextureAtlasSprite fluidTexture,
        int tint,
        int lightEmission,
        float xMin,
        float yMin,
        float zMin,
        float xMax,
        float yMax,
        float zMax,
        VertexConsumer builder,
        PoseStack.Pose entry,
        int light,
        boolean renderBottom,
        boolean invertGasses
    ) {
        int blockLightIn = light >> 4 & 0xF;
        int luminosity = Math.max(blockLightIn, lightEmission);
        light = light & 0xF00000 | luminosity << 4;

        //TODO
        //        Vec3 center = new Vec3(xMin + (xMax - xMin) / 2, yMin + (yMax - yMin) / 2, zMin + (zMax - zMin) / 2);
        //        if (invertGasses && false) {
        //            entry.translate((float) center.x, (float) center.y, (float) center.z);
        //            entry.rotate(Axis.XP.rotationDegrees(180));
        //            entry.translate((float) -center.x, (float) -center.y, (float) -center.z);
        //        }

        for (Direction side : Iterate.directions) {
            if (side == Direction.DOWN && !renderBottom) {
                continue;
            }

            boolean positive = side.getAxisDirection() == Direction.AxisDirection.POSITIVE;
            if (side.getAxis().isHorizontal()) {
                if (side.getAxis() == Direction.Axis.X) {
                    renderTiledFace(
                        side,
                        zMin,
                        yMin,
                        zMax,
                        yMax,
                        positive ? xMax : xMin,
                        builder,
                        entry,
                        light,
                        tint,
                        fluidTexture,
                        1
                    );
                } else {
                    renderTiledFace(
                        side,
                        xMin,
                        yMin,
                        xMax,
                        yMax,
                        positive ? zMax : zMin,
                        builder,
                        entry,
                        light,
                        tint,
                        fluidTexture,
                        1
                    );
                }
            } else {
                renderTiledFace(
                    side,
                    xMin,
                    zMin,
                    xMax,
                    zMax,
                    positive ? yMax : yMin,
                    builder,
                    entry,
                    light,
                    tint,
                    fluidTexture,
                    1
                );
            }
        }
    }

    public static void renderTiledFace(
        Direction dir,
        float left,
        float down,
        float right,
        float up,
        float depth,
        VertexConsumer builder,
        PoseStack.Pose entry,
        int light,
        int color,
        TextureAtlasSprite texture,
        float textureScale
    ) {
        boolean positive = dir.getAxisDirection() == Direction.AxisDirection.POSITIVE;
        boolean horizontal = dir.getAxis().isHorizontal();
        boolean x = dir.getAxis() == Direction.Axis.X;

        float f;
        float x2;
        float y2;
        float u1, u2;
        float v1, v2;
        for (float x1 = left; x1 < right; x1 = x2) {
            f = Mth.floor(x1);
            x2 = Math.min(f + 1, right);
            if (dir == Direction.NORTH || dir == Direction.EAST) {
                f = Mth.ceil(x2);
                u1 = texture.getU((f - x2) * textureScale);
                u2 = texture.getU((f - x1) * textureScale);
            } else {
                u1 = texture.getU((x1 - f) * textureScale);
                u2 = texture.getU((x2 - f) * textureScale);
            }
            for (float y1 = down; y1 < up; y1 = y2) {
                f = Mth.floor(y1);
                y2 = Math.min(f + 1, up);
                if (dir == Direction.UP) {
                    v1 = texture.getV((y1 - f) * textureScale);
                    v2 = texture.getV((y2 - f) * textureScale);
                } else {
                    f = Mth.ceil(y2);
                    v1 = texture.getV((f - y2) * textureScale);
                    v2 = texture.getV((f - y1) * textureScale);
                }

                if (horizontal) {
                    if (x) {
                        putVertex(builder, entry, depth, y2, positive ? x2 : x1, color, u1, v1, dir, light);
                        putVertex(builder, entry, depth, y1, positive ? x2 : x1, color, u1, v2, dir, light);
                        putVertex(builder, entry, depth, y1, positive ? x1 : x2, color, u2, v2, dir, light);
                        putVertex(builder, entry, depth, y2, positive ? x1 : x2, color, u2, v1, dir, light);
                    } else {
                        putVertex(builder, entry, positive ? x1 : x2, y2, depth, color, u1, v1, dir, light);
                        putVertex(builder, entry, positive ? x1 : x2, y1, depth, color, u1, v2, dir, light);
                        putVertex(builder, entry, positive ? x2 : x1, y1, depth, color, u2, v2, dir, light);
                        putVertex(builder, entry, positive ? x2 : x1, y2, depth, color, u2, v1, dir, light);
                    }
                } else {
                    putVertex(builder, entry, x1, depth, positive ? y1 : y2, color, u1, v1, dir, light);
                    putVertex(builder, entry, x1, depth, positive ? y2 : y1, color, u1, v2, dir, light);
                    putVertex(builder, entry, x2, depth, positive ? y2 : y1, color, u2, v2, dir, light);
                    putVertex(builder, entry, x2, depth, positive ? y1 : y2, color, u2, v1, dir, light);
                }
            }
        }
    }

    protected static void putVertex(
        VertexConsumer builder,
        PoseStack.Pose entry,
        float x,
        float y,
        float z,
        int color,
        float u,
        float v,
        Direction face,
        int light
    ) {
        Vec3i normal = face.getUnitVec3i();
        int a = color >> 24 & 0xff;
        int r = color >> 16 & 0xff;
        int g = color >> 8 & 0xff;
        int b = color & 0xff;
        builder.addVertex(entry.pose(), x, y, z).setColor(r, g, b, a).setUv(u, v).setLight(light)
            .setNormal(entry, normal.getX(), normal.getY(), normal.getZ());
    }

    public record FluidRenderState(RenderType layer, TextureAtlasSprite fluidTexture, int tint, int lightEmission,
                                   float xMin, float yMin, float zMin, float xMax, float yMax, float zMax,
                                   int lightCoords, boolean renderBottom,
                                   boolean invertGasses) implements SubmitNodeCollector.CustomGeometryRenderer {
        public void submit(PoseStack poseStack, SubmitNodeCollector queue) {
            queue.submitCustomGeometry(poseStack, layer, this);
        }

        @Override
        public void render(PoseStack.Pose pose, VertexConsumer vertexConsumer) {
            renderFluidBox(
                fluidTexture,
                tint,
                lightEmission,
                xMin,
                yMin,
                zMin,
                xMax,
                yMax,
                zMax,
                vertexConsumer,
                pose,
                lightCoords,
                renderBottom,
                invertGasses
            );
        }
    }
}
