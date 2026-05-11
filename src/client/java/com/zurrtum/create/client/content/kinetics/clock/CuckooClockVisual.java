package com.zurrtum.create.client.content.kinetics.clock;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.OrientedRotatingVisual;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstancerProvider;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.animation.AnimationBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.animation.CuckooClockAnimationBehaviour;
import com.zurrtum.create.content.kinetics.clock.CuckooClockBlockEntity;
import com.zurrtum.create.content.kinetics.clock.CuckooClockBlockEntity.Animation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getEastRotateAngle;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getUpRotateAngle;

public class CuckooClockVisual extends OrientedRotatingVisual<CuckooClockBlockEntity> implements SimpleDynamicVisual {
    private final TransformedInstance hourHand;
    private final TransformedInstance minuteHand;
    private final TransformedInstance leftDoor;
    private final TransformedInstance rightDoor;
    private @Nullable TransformedInstance figure;
    private final Matrix4f transform;

    public CuckooClockVisual(VisualizationContext context, CuckooClockBlockEntity blockEntity, float partialTick) {
        Direction facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        super(
            context,
            blockEntity,
            partialTick,
            Direction.SOUTH,
            facing.getOpposite(),
            Models.chunkPartial(AllPartialModels.SHAFT_HALF)
        );
        InstancerProvider instancerProvider = instancerProvider();
        hourHand = instancerProvider.instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(AllPartialModels.CUCKOO_HOUR_HAND)
        ).createInstance();
        minuteHand = instancerProvider.instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(AllPartialModels.CUCKOO_MINUTE_HAND)
        ).createInstance();
        leftDoor = instancerProvider.instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(AllPartialModels.CUCKOO_LEFT_DOOR)
        ).createInstance();
        rightDoor = instancerProvider.instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(AllPartialModels.CUCKOO_RIGHT_DOOR)
        ).createInstance();
        BlockPos visualPos = getVisualPosition();
        transform = new Matrix4f().translate(visualPos.getX(), visualPos.getY(), visualPos.getZ());
        Quaternionf rotate = getUpRotateAngle(AngleHelper.horizontalAngle(facing.getCounterClockWise()));
        if (rotate != null) {
            transform.rotateAround(rotate, 0.5f, 0.5f, 0.5f);
        }
        transformHandModels(partialTick);
        transformFigureModels(partialTick);
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector) {
        switch (blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH -> setSectionCollector(sectionCollector, 0, 0, -1, 0, 0, 0);
            case SOUTH -> setSectionCollector(sectionCollector, 0, 0, 0, 0, 0, 1);
            case WEST -> setSectionCollector(sectionCollector, -1, 0, 0, 0, 0, 0);
            case EAST -> setSectionCollector(sectionCollector, 0, 0, 0, 1, 0, 0);
        }
    }

    private void transformHandModels(float partialTick) {
        hourHand.setTransform(transform);
        minuteHand.setTransform(transform);
        CuckooClockAnimationBehaviour behaviour = (CuckooClockAnimationBehaviour) blockEntity.getBehaviour(
            AnimationBehaviour.TYPE);
        if (behaviour != null) {
            Quaternionf hourAngle = getEastRotateAngle(behaviour.hourHand.getValue(partialTick));
            if (hourAngle != null) {
                hourHand.rotateAround(hourAngle, 0.125f, 0.375f, 0.5f);
            }
            Quaternionf minuteAngle = getEastRotateAngle(behaviour.minuteHand.getValue(partialTick));
            if (minuteAngle != null) {
                minuteHand.rotateAround(minuteAngle, 0.125f, 0.375f, 0.5f);
            }
        }
        hourHand.setChanged();
        minuteHand.setChanged();
    }

    private void transformFigureModels(float partialTick) {
        rightDoor.setTransform(transform);
        leftDoor.setTransform(transform);
        float doorAngle = CuckooClockRenderer.getDoorAngle(blockEntity, partialTick);
        if (doorAngle != 0) {
            Quaternionf rotate = new Quaternionf().setAngleAxis(Mth.DEG_TO_RAD * doorAngle, 0, 1, 0);
            rightDoor.rotateAround(rotate, 0.125f, 0, 0.625f);
            leftDoor.rotateAround(rotate.conjugate(), 0.125f, 0, 0.375f);
            if (blockEntity.animationType != Animation.NONE) {
                float offset = -(doorAngle / 135) * 0.5f + 0.625f;
                if (offset <= 0.4f) {
                    if (figure == null) {
                        figure = instancerProvider().instancer(
                            InstanceTypes.TRANSFORMED,
                            Models.chunkPartial(
                                blockEntity.animationType == Animation.PIG ? AllPartialModels.CUCKOO_PIG :
                                    AllPartialModels.CUCKOO_CREEPER)
                        ).createInstance();
                    }
                    figure.setTransform(transform).translate(offset, 0, 0).setChanged();
                }
            }
        }
        rightDoor.setChanged();
        leftDoor.setChanged();
    }

    @Override
    public void beginFrame(Context ctx) {
        float partialTick = ctx.partialTick();
        transformHandModels(partialTick);
        if (blockEntity.animationProgress.settled()) {
            if (figure != null) {
                figure.delete();
                figure = null;
            }
        } else {
            transformFigureModels(partialTick);
        }
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        relight(hourHand, minuteHand, leftDoor, rightDoor);
    }

    @Override
    protected void _delete() {
        super._delete();
        hourHand.delete();
        minuteHand.delete();
        leftDoor.delete();
        rightDoor.delete();
        if (figure != null) {
            figure.delete();
        }
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(hourHand);
        consumer.accept(minuteHand);
        consumer.accept(leftDoor);
        consumer.accept(rightDoor);
    }
}
