package com.zurrtum.create.client.content.fluids.spout;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstancerProvider;
import com.zurrtum.create.client.flywheel.api.visual.ShaderLightVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.content.fluids.spout.SpoutBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class SpoutVisual extends AbstractBlockEntityVisual<SpoutBlockEntity> implements SimpleDynamicVisual, ShaderLightVisual {
    private final TransformedInstance middle;
    private final TransformedInstance bottom;
    private float prevOffset;

    public SpoutVisual(VisualizationContext ctx, SpoutBlockEntity blockEntity, float partialTick) {
        super(ctx, blockEntity, partialTick);
        InstancerProvider instancerProvider = instancerProvider();
        middle = instancerProvider.instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(AllPartialModels.SPOUT_MIDDLE)
        ).createInstance();
        bottom = instancerProvider.instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(AllPartialModels.SPOUT_BOTTOM)
        ).createInstance();
        transformModels(getOffset(blockEntity, partialTick));
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector) {
        setSectionCollector(sectionCollector, 0, -1, 0, 0, 0, 0);
    }

    private void transformModels(float offset) {
        BlockPos pos = getVisualPosition();
        int x = pos.getX();
        float y = pos.getY() + offset;
        int z = pos.getZ();
        middle.setIdentityTransform().translate(x, y, z).setChanged();
        bottom.setIdentityTransform().translate(x, y + offset, z).setChanged();
    }

    @Override
    public void beginFrame(Context ctx) {
        float offset = getOffset(blockEntity, ctx.partialTick());
        if (offset == prevOffset) {
            return;
        }
        prevOffset = offset;
        transformModels(offset);
    }

    private static float getOffset(SpoutBlockEntity blockEntity, float partialTick) {
        int processingTicks = blockEntity.processingTicks;
        float processingPT = processingTicks - partialTick;
        if (processingPT < 0) {
            return 0;
        }
        if (processingPT < 2) {
            return -3 * Mth.lerp(processingPT / 2.0f, 0, -1) / 32.0f;
        }
        if (processingPT < 10) {
            return 0.09375f;
        }
        SmartFluidTankBehaviour tank = blockEntity.tank;
        if (tank == null || tank.getPrimaryTank().getRenderedFluid().isEmpty()) {
            return 0;
        }
        return -3 * (float) (Math.pow(2 * Mth.clamp(1 - (processingPT - 5) / 10, 0, 1) - 1, 2) - 1) / 32.0f;
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        consumer.accept(middle);
        consumer.accept(bottom);
    }

    @Override
    public void updateLight(float partialTick) {
        relight(middle, bottom);
    }

    @Override
    protected void _delete() {
        middle.delete();
        bottom.delete();
    }
}
