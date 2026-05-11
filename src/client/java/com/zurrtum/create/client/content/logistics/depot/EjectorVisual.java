package com.zurrtum.create.client.content.logistics.depot;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.ShaftVisual;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.content.logistics.depot.EjectorBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.function.Consumer;

public class EjectorVisual extends ShaftVisual<EjectorBlockEntity> implements SimpleDynamicVisual {

    protected final TransformedInstance plate;

    private float lastProgress = Float.NaN;

    public EjectorVisual(VisualizationContext dispatcher, EjectorBlockEntity blockEntity, float partialTick) {
        super(dispatcher, blockEntity, partialTick);

        plate = instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(AllPartialModels.EJECTOR_TOP)
        ).createInstance();

        pivotPlate(getLidProgress(partialTick));
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector) {
        switch (blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH -> setSectionCollector(sectionCollector, 0, 0, 0, 0, 1, 1);
            case SOUTH -> setSectionCollector(sectionCollector, 0, 0, -1, 0, 1, 0);
            case EAST -> setSectionCollector(sectionCollector, 0, 0, 0, 1, 1, 0);
            case WEST -> setSectionCollector(sectionCollector, -1, 0, 0, 0, 1, 0);
        }
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        float lidProgress = getLidProgress(ctx.partialTick());

        if (lidProgress == lastProgress) {
            return;
        }

        pivotPlate(lidProgress);
        lastProgress = lidProgress;
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        relight(plate);
    }

    @Override
    protected void _delete() {
        super._delete();
        plate.delete();
    }

    private float getLidProgress(float pt) {
        return blockEntity.getLidProgress(pt);
    }

    private void pivotPlate(float lidProgress) {
        float angle = lidProgress * 70;

        EjectorRenderer.applyLidAngle(blockEntity, angle, plate.setIdentityTransform().translate(getVisualPosition()));
        plate.setChanged();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(plate);
    }
}