package com.zurrtum.create.client.content.kinetics.millstone;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.SingleAxisRotatingVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.content.kinetics.millstone.MillstoneBlockEntity;

public class MillstoneVisual extends SingleAxisRotatingVisual<MillstoneBlockEntity> {
    public MillstoneVisual(VisualizationContext context, MillstoneBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick, Models.chunkPartial(AllPartialModels.MILLSTONE_COG));
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector) {
        setSectionCollector(sectionCollector, -1, 0, -1, 1, 0, 1);
    }
}
