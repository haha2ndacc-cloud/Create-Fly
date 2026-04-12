package com.zurrtum.create.client.content.fluids.spout;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper.FluidRenderState;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.fluids.spout.SpoutRenderer.SpoutRenderState;
import com.zurrtum.create.content.fluids.spout.SpoutBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public class SpoutRenderer implements BlockEntityRenderer<SpoutBlockEntity, SpoutRenderState> {
    protected final FluidStateModelSet fluidStateModelSet;

    public SpoutRenderer(Context context) {
        fluidStateModelSet = context.blockModelResolver().modelManager.getFluidStateModelSet();
    }

    @Override
    public SpoutRenderState createRenderState() {
        return new SpoutRenderState();
    }

    @Override
    public void extractRenderState(
        SpoutBlockEntity be,
        SpoutRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        SmartFluidTankBehaviour tank = be.tank;
        if (tank == null) {
            return;
        }
        state.blockPos = be.getBlockPos();
        state.blockState = be.getBlockState();
        state.blockEntityType = be.getType();
        BlockAndTintGetter renderWorld = null;
        CardinalLighting cardinalLighting = null;
        Level world = be.getLevel();
        if (world != null) {
            state.lightCoords = LevelRenderer.getLightCoords(world, state.blockPos);
            if (world instanceof BlockAndTintGetter getter) {
                renderWorld = getter;
                cardinalLighting = getter.cardinalLighting();
            }
        } else {
            state.lightCoords = LightCoordsUtil.FULL_BRIGHT;
        }
        state.breakProgress = crumblingOverlay;
        TankSegment primaryTank = tank.getPrimaryTank();
        FluidStack fluidStack = primaryTank.getRenderedFluid();
        float radius = 0;
        int processingTicks = be.processingTicks;
        float processingPT = processingTicks - tickProgress;
        if (!fluidStack.isEmpty()) {
            float level = primaryTank.getFluidLevel().getValue(tickProgress);
            if (level != 0) {
                boolean top = false;//TODO fluidStack.getFluid().getFluidType().isLighterThanAir();
                float min = 0.15625f;
                float n = 0.6875f;
                float max = min + n;
                float yOffset = n * Math.max(level, 0.175f);
                float yMin = min - yOffset;
                state.offset = top ? max - min : yOffset;
                state.fluid = FluidRenderHelper.extractFluidRenderState(
                    renderWorld,
                    state.blockPos,
                    fluidStateModelSet,
                    fluidStack.getFluid(),
                    fluidStack.getComponentChanges(),
                    min,
                    yMin,
                    min,
                    max,
                    min,
                    max,
                    state.lightCoords,
                    false,
                    true
                );
            }
            if (processingTicks != -1) {
                float processingProgress = 1 - (processingPT - 5) / 10;
                processingProgress = Mth.clamp(processingProgress, 0, 1);
                radius = (float) (Math.pow(2 * processingProgress - 1, 2) - 1);
                AABB box = new AABB(0.5, 0.0, 0.5, 0.5, -1.2, 0.5).inflate(radius / 32.0f);
                state.process = FluidRenderHelper.extractFluidRenderState(
                    renderWorld,
                    state.blockPos,
                    fluidStateModelSet,
                    fluidStack.getFluid(),
                    fluidStack.getComponentChanges(),
                    (float) box.minX,
                    (float) box.minY,
                    (float) box.minZ,
                    (float) box.maxX,
                    (float) box.maxY,
                    (float) box.maxZ,
                    state.lightCoords,
                    true,
                    true
                );
            }
        }
        float squeeze;
        if (processingPT < 0) {
            squeeze = 0;
        } else if (processingPT < 2) {
            squeeze = Mth.lerp(processingPT / 2.0f, 0, -1);
        } else if (processingPT < 10) {
            squeeze = -1;
        } else {
            squeeze = radius;
        }
        state.top = CachedBuffers.partial(AllPartialModels.SPOUT_TOP, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        state.middle = CachedBuffers.partial(AllPartialModels.SPOUT_MIDDLE, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        state.bottom = CachedBuffers.partial(AllPartialModels.SPOUT_BOTTOM, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        state.bitOffset = -3 * squeeze / 32.0f;
    }

    @Override
    public void submit(
        SpoutRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        matrices.pushPose();
        state.top.submit(matrices, queue);
        matrices.translate(0, state.bitOffset, 0);
        state.middle.submit(matrices, queue);
        matrices.translate(0, state.bitOffset, 0);
        state.bottom.submit(matrices, queue);
        matrices.popPose();
        if (state.process != null) {
            state.process.submit(matrices, queue);
        }
        if (state.fluid != null) {
            matrices.translate(0, state.offset, 0);
            state.fluid.submit(matrices, queue);
        }
    }

    public static class SpoutRenderState extends BlockEntityRenderState {
        public float bitOffset;
        public @UnknownNullability SuperByteBufferRenderState top;
        public @UnknownNullability SuperByteBufferRenderState middle;
        public @UnknownNullability SuperByteBufferRenderState bottom;
        public float offset;
        public @Nullable FluidRenderState fluid;
        public @Nullable FluidRenderState process;
    }
}
