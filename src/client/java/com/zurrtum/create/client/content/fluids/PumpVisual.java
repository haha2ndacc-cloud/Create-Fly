package com.zurrtum.create.client.content.fluids;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.SingleAxisRotatingVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.content.fluids.pump.PumpBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class PumpVisual extends SingleAxisRotatingVisual<PumpBlockEntity> {
    public PumpVisual(VisualizationContext context, PumpBlockEntity blockEntity, float partialTick) {
        super(
            context,
            blockEntity,
            partialTick,
            Direction.SOUTH,
            Models.chunkPartial(AllPartialModels.MECHANICAL_PUMP_COG)
        );
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector) {
        switch (blockState.getValue(BlockStateProperties.FACING).getAxis()) {
            case X -> setSectionCollector(sectionCollector, 0, -1, -1, 0, 1, 1);
            case Y -> setSectionCollector(sectionCollector, -1, 0, -1, 1, 0, 1);
            case Z -> setSectionCollector(sectionCollector, -1, -1, 0, 1, 1, 0);
        }
    }
}
