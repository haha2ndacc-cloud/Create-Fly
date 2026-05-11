package com.zurrtum.create.client.content.kinetics.turntable;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.SingleAxisRotatingVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.content.kinetics.turntable.TurntableBlockEntity;

public class TurntableVisual extends SingleAxisRotatingVisual<TurntableBlockEntity> {
    public TurntableVisual(VisualizationContext context, TurntableBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick, Models.chunkPartial(AllPartialModels.TURNTABLE));
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector) {
        setSectionCollector(sectionCollector, -1, 0, -1, 1, 0, 1);
    }
}
