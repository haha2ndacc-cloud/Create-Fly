package com.zurrtum.create.client.foundation.blockEntity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.ColoredOverlayBlockEntityRenderer.ColoredOverlayRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public abstract class ColoredOverlayBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, ColoredOverlayRenderState> {
    public ColoredOverlayBlockEntityRenderer(Context context) {
    }

    @Override
    public ColoredOverlayRenderState createRenderState() {
        return new ColoredOverlayRenderState();
    }

    @Override
    public void extractRenderState(
        T be,
        ColoredOverlayRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        state.model = getOverlayBuffer(be, state).cardinalLighting(level).light(state.lightCoords)
            .color(getColor(be, tickProgress)).extractRenderState();
    }

    @Override
    public void submit(
        ColoredOverlayRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        state.model.submit(matrices, queue);
    }

    protected abstract int getColor(T be, float partialTicks);

    protected abstract SuperByteBuffer getOverlayBuffer(T be, ColoredOverlayRenderState state);

    public static class ColoredOverlayRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState model;
    }
}
