package com.zurrtum.create.client.content.fluids.tank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper.FluidRenderState;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.content.fluids.tank.FluidTankBlockEntity;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public class FluidTankRenderer implements BlockEntityRenderer<FluidTankBlockEntity, FluidTankRenderer.FluidTankRenderState> {
    protected final FluidStateModelSet fluidStateModelSet;

    public FluidTankRenderer(BlockEntityRendererProvider.Context context) {
        fluidStateModelSet = context.blockModelResolver().modelManager.getFluidStateModelSet();
    }

    @Override
    public FluidTankRenderState createRenderState() {
        return new FluidTankRenderState();
    }

    @Override
    public void extractRenderState(
        FluidTankBlockEntity be,
        FluidTankRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        if (!be.isController()) {
            return;
        }
        if (be.window) {
            updateFluidTankState(be, state, tickProgress, crumblingOverlay);
        } else if (be.boiler.isActive()) {
            updateBoilerState(be, state, tickProgress, crumblingOverlay);
        }
    }

    public void updateFluidTankState(
        FluidTankBlockEntity be,
        FluidTankRenderState state,
        float tickProgress,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        LerpedFloat fluidLevel = be.getFluidLevel();
        if (fluidLevel == null) {
            return;
        }
        float capHeight = 1 / 4f;
        float minPuddleHeight = 1 / 16f;
        float totalHeight = be.getHeight() - 2 * capHeight - minPuddleHeight;
        float level = fluidLevel.getValue(tickProgress);
        if (level < 1 / (512f * totalHeight)) {
            return;
        }
        FluidStack fluidStack = be.getTankInventory().getFluid();
        if (fluidStack.isEmpty()) {
            return;
        }
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        float clampedLevel = Mth.clamp(level * totalHeight, 0, totalHeight);
        state.translate = clampedLevel - totalHeight;

        //TODO
        boolean top = false;//fluidStack.getFluid()
        //			.getFluidType()
        //            .isLighterThanAir();

        int width = be.getWidth();
        float xMin = 1 / 16f + 1 / 128f;
        float xMax = xMin + width - 2 * xMin;
        float yMin = totalHeight + capHeight + minPuddleHeight - clampedLevel;
        float yMax = yMin + clampedLevel;

        if (top) {
            yMin += totalHeight - clampedLevel;
            yMax += totalHeight - clampedLevel;
        }

        float zMax = xMin + width - 2 * xMin;
        BlockAndTintGetter world = (BlockAndTintGetter) be.getLevel();
        state.fluid = FluidRenderHelper.extractFluidRenderState(
            world,
            state.blockPos,
            fluidStateModelSet,
            fluidStack.getFluid(),
            fluidStack.getComponentChanges(),
            xMin,
            yMin,
            xMin,
            xMax,
            yMax,
            zMax,
            state.lightCoords,
            false,
            true
        );
    }

    public void updateBoilerState(
        FluidTankBlockEntity be,
        FluidTankRenderState state,
        float tickProgress,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        boolean[] occludedDirections = be.boiler.occludedDirections;
        if (occludedDirections[0] && occludedDirections[1] && occludedDirections[2] && occludedDirections[3]) {
            return;
        }
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.translate = be.getWidth() / 2f;
        BoilerRenderState data = new BoilerRenderState();
        state.boiler = data;
        data.layer = RenderTypes.cutoutMovingBlock();
        data.light = state.lightCoords;
        data.translateX = state.translate - 6 / 16f;
        data.dialPivotY = 6f / 16;
        data.dialPivotZ = 8f / 16;
        data.progress = -145 * be.boiler.gauge.getValue(tickProgress) + 90;
        data.gauge = CachedBuffers.partial(AllPartialModels.BOILER_GAUGE, state.blockState);
        data.gaugeDial = CachedBuffers.partial(AllPartialModels.BOILER_GAUGE_DIAL, state.blockState);
        data.south = !occludedDirections[0];
        data.west = !occludedDirections[1];
        data.north = !occludedDirections[2];
        data.east = !occludedDirections[3];
    }

    @Override
    public void submit(
        FluidTankRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.fluid != null) {
            matrices.translate(0, state.translate, 0);
            state.fluid.submit(queue, matrices);
        } else if (state.boiler != null) {
            state.boiler.submit(queue, matrices, state.translate);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(/*FluidTankBlockEntity be*/) {
        //TODO
        //        return be.isController();
        return true;
    }

    public static class FluidTankRenderState extends BlockEntityRenderState {
        public float translate;
        public @Nullable FluidRenderState fluid;
        public @Nullable BoilerRenderState boiler;
    }

    public static class BoilerRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        @UnknownNullability
        public RenderType layer;
        @UnknownNullability
        public SuperByteBuffer gauge, gaugeDial;
        public float translateX, dialPivotY, dialPivotZ, progress;
        public boolean south, west, north, east;
        public int light;

        public void submit(SubmitNodeCollector queue, PoseStack poseStack, float translate) {
            poseStack.translate(translate, 0.5, translate);
            queue.submitCustomGeometry(poseStack, layer, this);
        }

        public void render(int yRot, PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            gauge.rotateYDegrees(yRot).uncenter().translate(translateX, 0, 0).light(light)
                .renderInto(matricesEntry, vertexConsumer);
            gaugeDial.rotateYDegrees(yRot).uncenter().translate(translateX, 0, 0).translate(0, dialPivotY, dialPivotZ)
                .rotateXDegrees(progress).translate(0, -dialPivotY, -dialPivotZ).light(light)
                .renderInto(matricesEntry, vertexConsumer);
        }

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            if (south) {
                render(-90, matricesEntry, vertexConsumer);
            }
            if (west) {
                render(-180, matricesEntry, vertexConsumer);
            }
            if (north) {
                render(-270, matricesEntry, vertexConsumer);
            }
            if (east) {
                render(-360, matricesEntry, vertexConsumer);
            }
        }
    }
}
