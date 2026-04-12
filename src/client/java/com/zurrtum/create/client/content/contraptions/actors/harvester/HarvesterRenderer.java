package com.zurrtum.create.client.content.contraptions.actors.harvester;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.contraptions.actors.harvester.HarvesterRenderer.HarvesterRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.contraptions.actors.harvester.HarvesterBlock;
import com.zurrtum.create.content.contraptions.actors.harvester.HarvesterBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getUpRotateAngle;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getWestRotateAngle;

public class HarvesterRenderer implements BlockEntityRenderer<HarvesterBlockEntity, HarvesterRenderState> {
    public HarvesterRenderer(Context context) {
    }

    @Override
    public HarvesterRenderState createRenderState() {
        return new HarvesterRenderState();
    }

    @Override
    public void extractRenderState(
        HarvesterBlockEntity be,
        HarvesterRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        state.model = CachedBuffers.partial(AllPartialModels.HARVESTER_BLADE, state.blockState).cardinalLighting(level)
            .light(state.lightCoords).extractRenderState();
        state.angle = getWestRotateAngle(AnimationTickHolder.getRenderTime(level) / 20 * be.getAnimatedSpeed() % 360);
        state.horizontalAngle = getUpRotateAngle(AngleHelper.horizontalAngle(state.blockState.getValue(HarvesterBlock.FACING)));
    }

    @Override
    public void submit(
        HarvesterRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.horizontalAngle != null) {
            matrices.rotateAround(state.horizontalAngle, 0.5f, 0.5f, 0.5f);
        }
        if (state.angle != null) {
            matrices.rotateAround(state.angle, 0, 0.375f, 0.5625f);
        }
        state.model.submit(matrices, queue);
    }

    public static class HarvesterRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState model;
        public @Nullable Quaternionf angle;
        public @Nullable Quaternionf horizontalAngle;
    }
}
