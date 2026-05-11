package com.zurrtum.create.client.content.equipment.armor;

import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.content.kinetics.base.RotatingInstance;
import com.zurrtum.create.client.content.kinetics.base.RotatingPivotInstance;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstancerProvider;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleTickableVisual;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.content.equipment.armor.BacktankBlock;
import com.zurrtum.create.content.equipment.armor.BacktankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class BacktankVisual extends KineticBlockEntityVisual<BacktankBlockEntity> implements SimpleTickableVisual {
    protected static final float COG_SPEED_MULTIPLIER = 5;
    protected final RotatingInstance rotatingModel;
    protected final RotatingPivotInstance cogs;

    public BacktankVisual(VisualizationContext context, BacktankBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);
        InstancerProvider instancerProvider = instancerProvider();
        float speed = blockEntity.getSpeed();
        BlockPos visualPos = getVisualPosition();
        rotatingModel = instancerProvider.instancer(
                AllInstanceTypes.ROTATING,
                Models.chunkPartial(BacktankRenderer.getShaftModel(blockEntity.getBlockState()))
            ).createInstance().setRotationAxis(Direction.UP.step())
            .setRotationalSpeed(speed * RotatingInstance.SPEED_MULTIPLIER)
            .setRotationOffset(rotationOffset(blockState, Axis.Y, pos)).setPosition(visualPos);
        rotatingModel.setChanged();
        cogs = instancerProvider.instancer(
            AllInstanceTypes.ROTATING_PIVOT,
            Models.chunkPartial(BacktankRenderer.getCogsModel(blockState))
        ).createInstance();
        Direction facing = blockState.getValue(BacktankBlock.HORIZONTAL_FACING);
        switch (facing) {
            case NORTH -> cogs.pivot(-0.5f, -0.09375f, 0.1875f);
            case SOUTH -> cogs.pivot(-0.5f, -0.09375f, -0.1875f);
            case EAST -> cogs.pivot(-0.1875f, -0.09375f, -0.5f);
            case WEST -> cogs.pivot(0.1875f, -0.09375f, -0.5f);
        }
        cogs.rotateToFace(Direction.NORTH, facing).setRotationAxis(facing.getClockWise().step())
            .setRotationalSpeed(speed * COG_SPEED_MULTIPLIER).setPosition(visualPos).setChanged();
    }

    @Override
    public void update(float pt) {
        float speed = blockEntity.getSpeed();
        rotatingModel.setRotationalSpeed(speed * RotatingInstance.SPEED_MULTIPLIER).setChanged();
        cogs.setRotationalSpeed(speed * COG_SPEED_MULTIPLIER).setChanged();
    }

    @Override
    public void tick(Context context) {
        applyOverstressEffect(blockEntity, rotatingModel);
    }

    @Override
    public void updateLight(float partialTick) {
        relight(rotatingModel, cogs);
    }

    @Override
    protected void _delete() {
        rotatingModel.delete();
        cogs.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        consumer.accept(rotatingModel);
        consumer.accept(cogs);
    }
}
