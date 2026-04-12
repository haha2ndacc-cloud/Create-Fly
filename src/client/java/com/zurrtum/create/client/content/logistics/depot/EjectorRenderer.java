package com.zurrtum.create.client.content.logistics.depot;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.logistics.depot.DepotRenderer.DepotItemState;
import com.zurrtum.create.client.content.logistics.depot.DepotRenderer.DepotOutputItemState;
import com.zurrtum.create.client.content.logistics.depot.EjectorRenderer.EjectorRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.transform.Rotate;
import com.zurrtum.create.client.flywheel.lib.transform.Translate;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.logistics.depot.DepotBehaviour;
import com.zurrtum.create.content.logistics.depot.EjectorBlock;
import com.zurrtum.create.content.logistics.depot.EjectorBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;

public class EjectorRenderer implements BlockEntityRenderer<EjectorBlockEntity, EjectorRenderState> {
    static final Vec3 pivot = VecHelper.voxelSpace(0, 11.25, 0.75);
    protected final ItemModelResolver itemModelManager;

    public EjectorRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public EjectorRenderState createRenderState() {
        return new EjectorRenderState();
    }

    @Override
    public void extractRenderState(
        EjectorBlockEntity be,
        EjectorRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = be.getLevel();
        if (VisualizationManager.supportsVisualization(level)) {
            DepotBehaviour behaviour = be.getBehaviour(DepotBehaviour.TYPE);
            if (behaviour == null || behaviour.isEmpty()) {
                return;
            }
            state.incoming = DepotRenderer.createIncomingStateList(behaviour, itemModelManager, tickProgress, level);
            state.outputs = DepotRenderer.createOutputStateList(behaviour, itemModelManager, level);
            if (state.incoming != null || state.outputs != null) {
                SmartBlockEntityRenderer.extractBase(level, be, state, crumblingOverlay);
                state.lidAngle = getXRotateAngle(be.getLidProgress(tickProgress) * -70);
                Direction direction = state.blockState.getValue(EjectorBlock.HORIZONTAL_FACING);
                float yRot = 180 + AngleHelper.horizontalAngle(direction);
                if (yRot != 0) {
                    yRot *= Mth.DEG_TO_RAD;
                    state.yRot = new Quaternionf().rotationY(yRot);
                    state.yRotBack = new Quaternionf().rotationY(-yRot);
                }
            }
        } else {
            SmartBlockEntityRenderer.extractBase(level, be, state, crumblingOverlay);
            CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
            Direction direction = state.blockState.getValue(EjectorBlock.HORIZONTAL_FACING);
            Axis axis = direction.getClockWise().getAxis();
            state.model = CachedBuffers.block(KINETIC_BLOCK, shaft(axis)).cardinalLighting(cardinalLighting)
                .light(state.lightCoords).color(getTintColor(be)).extractRenderState();
            state.angle = getRotateAngleWithoutBeOffset(axis, be, state, level);
            state.top = CachedBuffers.partial(AllPartialModels.EJECTOR_TOP, state.blockState)
                .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
            state.lidAngle = getXRotateAngle(be.getLidProgress(tickProgress) * -70);
            float yRot = 180 + AngleHelper.horizontalAngle(direction);
            if (yRot != 0) {
                yRot *= Mth.DEG_TO_RAD;
                state.yRot = new Quaternionf().rotationY(yRot);
            }
            DepotBehaviour behaviour = be.getBehaviour(DepotBehaviour.TYPE);
            if (behaviour == null || behaviour.isEmpty()) {
                return;
            }
            state.incoming = DepotRenderer.createIncomingStateList(behaviour, itemModelManager, tickProgress, level);
            state.outputs = DepotRenderer.createOutputStateList(behaviour, itemModelManager, level);
            state.yRotBack = state.yRot != null ? new Quaternionf().rotationY(-yRot) : null;
        }
    }

    @Override
    public void submit(
        EjectorRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.model != null) {
            if (state.angle != null) {
                matrices.pushPose();
                matrices.rotateAround(state.angle, 0.5f, 0.5f, 0.5f);
                state.model.submit(matrices, queue);
                matrices.popPose();
            } else {
                state.model.submit(matrices, queue);
            }
        }
        if (state.yRot != null) {
            matrices.rotateAround(state.yRot, 0.5f, 0.5f, 0.5f);
        }
        if (state.lidAngle != null) {
            matrices.rotateAround(state.lidAngle, 0, (float) pivot.y, (float) pivot.z);
        }
        if (state.top != null) {
            state.top.submit(matrices, queue);
        }
        if (state.incoming != null || state.outputs != null) {
            if (state.yRotBack != null) {
                matrices.rotateAround(state.yRotBack, 0.5f, 0.5f, 0.5f);
            }
            DepotRenderer.renderItemsOf(
                state.incoming,
                state.outputs,
                state.blockPos,
                cameraState.pos,
                queue,
                matrices,
                state.lightCoords
            );
        }
    }

    static <T extends Translate<T> & Rotate<T>> void applyLidAngle(KineticBlockEntity be, float angle, T tr) {
        tr.center().rotateYDegrees(180 + AngleHelper.horizontalAngle(be.getBlockState()
                .getValue(EjectorBlock.HORIZONTAL_FACING))).uncenter().translate(pivot).rotateXDegrees(-angle)
            .translateBack(pivot);
    }

    public static class EjectorRenderState extends BlockEntityRenderState {
        public @Nullable SuperByteBufferRenderState model;
        public @Nullable Quaternionf angle;
        public @Nullable Quaternionf lidAngle;
        public @Nullable Quaternionf yRot;
        public @Nullable Quaternionf yRotBack;
        public @Nullable SuperByteBufferRenderState top;
        public DepotItemState @Nullable [] incoming;
        public @Nullable List<DepotOutputItemState> outputs;
    }
}
