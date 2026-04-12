package com.zurrtum.create.client.content.logistics;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstancerProvider;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.transform.Translate;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getXRotateAngle;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getYRotateAngle;

public class FlapStuffs {
    public static final int FLAP_COUNT = 4;
    public static final float X_OFFSET = 0.075f / 16.0f;
    public static final float SEGMENT_STEP = -3.05f / 16.0f;
    public static final Vec3 TUNNEL_PIVOT = VecHelper.voxelSpace(0, 10, 1.0f);
    public static final Vec3 FUNNEL_PIVOT = VecHelper.voxelSpace(0, 10, 9.5f);

    public static FlapsRenderState getFlapsRenderState(
        SuperByteBufferRenderState flapBuffer,
        Vec3 pivot,
        Direction funnelFacing,
        float flapness,
        float zOffset
    ) {
        float horizontalAngle = AngleHelper.horizontalAngle(funnelFacing.getOpposite());
        @Nullable Quaternionf[] angles = new Quaternionf[FLAP_COUNT];
        for (int segment = 0; segment < FLAP_COUNT; segment++) {
            float angle = flapAngle(flapness, segment);
            angles[segment] = getXRotateAngle(angle);
        }
        return new FlapsRenderState(flapBuffer, pivot, zOffset, getYRotateAngle(horizontalAngle), angles);
    }

    public static float flapAngle(float flapness, int segment) {
        float intensity = segment == 3 ? 1.5f : segment + 1;
        float abs = Math.abs(flapness);
        float flapAngle = Mth.sin((float) ((1 - abs) * Math.PI * intensity)) * 30 * flapness;
        if (flapness < 0) {
            flapAngle *= 0.5f;
        }
        return flapAngle;
    }

    public static Matrix4f commonTransform(BlockPos visualPosition, Direction side, float baseZOffset) {
        float horizontalAngle = AngleHelper.horizontalAngle(side.getOpposite());

        return new Matrix4f().translate(visualPosition.getX(), visualPosition.getY(), visualPosition.getZ())
            .translate(Translate.CENTER, Translate.CENTER, Translate.CENTER).rotateY(Mth.DEG_TO_RAD * horizontalAngle)
            .translate(-Translate.CENTER, -Translate.CENTER, -Translate.CENTER).translate(X_OFFSET, 0, baseZOffset);
    }

    public static class Visual {
        private final TransformedInstance[] flaps;

        private final Matrix4f commonTransform = new Matrix4f();
        private final Vec3 pivot;

        public Visual(InstancerProvider instancerProvider, Matrix4fc commonTransform, Vec3 pivot, Model flapModel) {
            this.pivot = pivot;
            this.commonTransform.set(commonTransform).translate((float) pivot.x, (float) pivot.y, (float) pivot.z);

            flaps = new TransformedInstance[FLAP_COUNT];

            instancerProvider.instancer(InstanceTypes.TRANSFORMED, flapModel).createInstances(flaps);
        }

        public void update(float f) {
            for (int segment = 0; segment < FLAP_COUNT; segment++) {
                var flap = flaps[segment];

                flap.setTransform(commonTransform).rotateXDegrees(flapAngle(f, segment)).translateBack(pivot)
                    .translate(segment * SEGMENT_STEP, 0, 0).setChanged();
            }
        }

        public void delete() {
            for (TransformedInstance flap : flaps) {
                flap.delete();
            }
        }

        public void updateLight(int light) {
            for (TransformedInstance flap : flaps) {
                flap.light(light).setChanged();
            }
        }

        public void collectCrumblingInstances(Consumer<Instance> consumer) {
            for (TransformedInstance flap : flaps) {
                consumer.accept(flap);
            }
        }
    }

    public record FlapsRenderState(SuperByteBufferRenderState model, Vec3 pivot, float zOffset,
                                   @Nullable Quaternionf horizontalAngle, @Nullable Quaternionf[] angles) {
        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            matrices.pushPose();
            if (horizontalAngle != null) {
                matrices.rotateAround(horizontalAngle, 0.5f, 0.5f, 0.5f);
            }
            matrices.translate(X_OFFSET, 0, zOffset);
            for (int segment = 0; segment < FLAP_COUNT; segment++) {
                Quaternionf angle = angles[segment];
                if (angle != null) {
                    matrices.pushPose();
                    matrices.rotateAround(angle, (float) pivot.x, (float) pivot.y, (float) pivot.z);
                    model.submit(matrices, queue);
                    matrices.popPose();
                } else {
                    model.submit(matrices, queue);
                }
                matrices.translate(SEGMENT_STEP, 0, 0);
            }
            matrices.popPose();
        }
    }
}
