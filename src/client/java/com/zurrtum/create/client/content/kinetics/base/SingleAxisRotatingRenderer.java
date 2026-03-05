package com.zurrtum.create.client.content.kinetics.base;

import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.KineticRenderState;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class SingleAxisRotatingRenderer extends KineticBlockEntityRenderer<KineticBlockEntity, KineticRenderState> {
    private final PartialModel model;

    public SingleAxisRotatingRenderer(BlockEntityRendererProvider.Context context, PartialModel model) {
        super(context);
        this.model = model;
    }

    public static BlockEntityRendererProvider<KineticBlockEntity, KineticRenderState> of(PartialModel model) {
        return (context) -> new SingleAxisRotatingRenderer(context, model);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(KineticBlockEntity be, KineticRenderState state) {
        return CachedBuffers.partialFacingVertical(model, state.blockState, state.direction);
    }
}
