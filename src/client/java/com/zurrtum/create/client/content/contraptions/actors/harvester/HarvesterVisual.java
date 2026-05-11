package com.zurrtum.create.client.content.contraptions.actors.harvester;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visual.ShaderLightVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.OrientedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.content.contraptions.actors.harvester.HarvesterBlock;
import com.zurrtum.create.content.contraptions.actors.harvester.HarvesterBlockEntity;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getUpRotateAngle;

// Ponder does not support Visual, so it does not require animation.
public class HarvesterVisual extends AbstractBlockEntityVisual<HarvesterBlockEntity> implements ShaderLightVisual {
    private final OrientedInstance harvester;

    public HarvesterVisual(VisualizationContext ctx, HarvesterBlockEntity blockEntity, float partialTick) {
        super(ctx, blockEntity, partialTick);
        harvester = instancerProvider().instancer(
            InstanceTypes.ORIENTED,
            Models.chunkPartial(AllPartialModels.HARVESTER_BLADE)
        ).createInstance().position(getVisualPosition());
        Quaternionf angle = getUpRotateAngle(AngleHelper.horizontalAngle(blockState.getValue(HarvesterBlock.FACING)));
        if (angle != null) {
            harvester.rotate(angle);
        }
        harvester.setChanged();
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        consumer.accept(harvester);
    }

    @Override
    public void updateLight(float partialTick) {
        relight(harvester);
    }

    @Override
    protected void _delete() {
        harvester.delete();
    }
}
