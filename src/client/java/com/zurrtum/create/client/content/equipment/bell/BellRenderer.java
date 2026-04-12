package com.zurrtum.create.client.content.equipment.bell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.equipment.bell.BellRenderer.BellRenderState;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.equipment.bell.AbstractBellBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getRadiansRotateAngle;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getUpRotateAngle;

public class BellRenderer<BE extends AbstractBellBlockEntity> implements BlockEntityRenderer<BE, BellRenderState> {
    private final PartialModel model;

    public BellRenderer(Context context, PartialModel model) {
        this.model = model;
    }

    public static <BE extends AbstractBellBlockEntity> BlockEntityRendererProvider<BE, BellRenderState> of(PartialModel model) {
        return context -> new BellRenderer<>(context, model);
    }

    @Override
    public BellRenderState createRenderState() {
        return new BellRenderState();
    }

    @Override
    public void extractRenderState(
        BE be,
        BellRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        state.model = CachedBuffers.partial(model, state.blockState).cardinalLighting(level).light(state.lightCoords)
            .extractRenderState();
        if (be.isRinging) {
            Direction direction = be.ringDirection.getCounterClockWise();
            state.angle = getRadiansRotateAngle(getSwingAngle(be.ringingTicks + tickProgress), direction);
        }
        Direction facing = state.blockState.getValue(BellBlock.FACING);
        BellAttachType attachment = state.blockState.getValue(BellBlock.ATTACHMENT);
        if (attachment == BellAttachType.SINGLE_WALL || attachment == BellAttachType.DOUBLE_WALL) {
            state.upAngle = getUpRotateAngle(AngleHelper.horizontalAngle(facing) + 90);
        } else {
            state.upAngle = getUpRotateAngle(AngleHelper.horizontalAngle(facing));
        }
    }

    @Override
    public void submit(
        BellRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.angle != null) {
            matrices.rotateAround(state.angle, 0.5f, 0.5f, 0.5f);
        }
        if (state.upAngle != null) {
            matrices.rotateAround(state.upAngle, 0.5f, 0.5f, 0.5f);
        }
        state.model.submit(matrices, queue);
    }

    public static float getSwingAngle(float time) {
        float t = time / 1.5f;
        return 1.2f * Mth.sin(t / (float) Math.PI) / (2.5f + t / 3.0f);
    }

    public static class BellRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState model;
        public @Nullable Quaternionf angle;
        public @Nullable Quaternionf upAngle;
    }
}
