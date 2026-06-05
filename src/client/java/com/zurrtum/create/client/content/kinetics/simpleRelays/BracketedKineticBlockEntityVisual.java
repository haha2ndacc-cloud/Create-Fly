package com.zurrtum.create.client.content.kinetics.simpleRelays;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.RotatingInstance;
import com.zurrtum.create.client.content.kinetics.base.SingleAxisRotatingVisual;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.api.visual.BlockEntityVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.zurrtum.create.content.kinetics.simpleRelays.ICogWheel;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.function.Consumer;

public class BracketedKineticBlockEntityVisual {
    public static BlockEntityVisual<BracketedKineticBlockEntity> create(
        VisualizationContext context,
        BracketedKineticBlockEntity blockEntity,
        float partialTick
    ) {
        if (ICogWheel.isLargeCog(blockEntity.getBlockState())) {
            return new LargeCogVisual(context, blockEntity, partialTick);
        }
        if (blockEntity.getBlockState().is(AllBlocks.COGWHEEL)) {
            return new CogVisual(context, blockEntity, partialTick, Models.chunkPartial(AllPartialModels.COGWHEEL));
        }
        return new SingleAxisRotatingVisual<>(
            context,
            blockEntity,
            partialTick,
            Models.chunkPartial(AllPartialModels.SHAFT)
        );
    }

    public static class CogVisual extends SingleAxisRotatingVisual<BracketedKineticBlockEntity> {
        public CogVisual(
            VisualizationContext context,
            BracketedKineticBlockEntity blockEntity,
            float partialTick,
            Model model
        ) {
            super(context, blockEntity, partialTick, model);
        }

        @Override
        public void setSectionCollector(SectionCollector sectionCollector) {
            switch (blockState.getValue(BlockStateProperties.AXIS)) {
                case X -> setSectionCollector(sectionCollector, 0, -1, -1, 0, 1, 1);
                case Y -> setSectionCollector(sectionCollector, -1, 0, -1, 1, 0, 1);
                default -> setSectionCollector(sectionCollector, -1, -1, 0, 1, 1, 0);
            }
        }
    }

    // Large cogs sometimes have to offset their teeth by 11.25 degrees in order to
    // mesh properly
    public static class LargeCogVisual extends CogVisual {

        protected final RotatingInstance additionalShaft;

        private LargeCogVisual(
            VisualizationContext context,
            BracketedKineticBlockEntity blockEntity,
            float partialTick
        ) {
            super(context, blockEntity, partialTick, Models.chunkPartial(AllPartialModels.SHAFTLESS_LARGE_COGWHEEL));

            Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(blockEntity);

            additionalShaft = instancerProvider().instancer(
                AllInstanceTypes.ROTATING,
                Models.chunkPartial(AllPartialModels.COGWHEEL_SHAFT)
            ).createInstance();

            additionalShaft.rotateToFace(axis).setup(blockEntity)
                .setRotationOffset(BracketedKineticBlockEntityRenderer.getShaftAngleOffset(axis, pos))
                .setPosition(getVisualPosition()).setChanged();
        }

        @Override
        public void update(float pt) {
            super.update(pt);
            additionalShaft.setup(blockEntity)
                .setRotationOffset(BracketedKineticBlockEntityRenderer.getShaftAngleOffset(rotationAxis(), pos))
                .setChanged();
        }

        @Override
        public void updateLight(float partialTick) {
            super.updateLight(partialTick);
            relight(additionalShaft);
        }

        @Override
        protected void _delete() {
            super._delete();
            additionalShaft.delete();
        }

        @Override
        public void collectCrumblingInstances(Consumer<Instance> consumer) {
            super.collectCrumblingInstances(consumer);
            consumer.accept(additionalShaft);
        }
    }
}
