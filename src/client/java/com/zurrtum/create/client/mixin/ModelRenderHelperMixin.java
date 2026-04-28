package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.flywheel.lib.model.baked.BufferEmitterOutput;
import com.zurrtum.create.client.flywheel.lib.model.baked.FabricEmitterSupplier;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelRenderHelper;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelRenderHelper.ThreadLocalObjects;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadTransform;
import net.fabricmc.fabric.api.client.renderer.v1.render.AltModelBlockRenderer;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ModelRenderHelper.class)
public class ModelRenderHelperMixin {
    @Shadow
    private static @UnknownNullability ModelConsumer CULL_INSTANCE;
    @Shadow
    private static @UnknownNullability ModelConsumer INSTANCE;
    @Shadow
    private static @UnknownNullability ModelConsumer AO_CULL_INSTANCE;
    @Shadow
    private static @UnknownNullability ModelConsumer AO_INSTANCE;

    @Overwrite(remap = false)
    public static void onReloadLevelRenderer() {
        Minecraft mc = Minecraft.getInstance();
        boolean ao = mc.options.ambientOcclusion().get();
        BlockColors blockColors = mc.getBlockColors();
        Renderer renderer = Renderer.get();
        AltModelBlockRenderer aoRender = renderer.altModelBlockRenderer(ao, false, blockColors);
        AltModelBlockRenderer cullRender = renderer.altModelBlockRenderer(ao, true, blockColors);
        INSTANCE = new Consumer(aoRender);
        CULL_INSTANCE = new Consumer(cullRender);
        if (ao) {
            AO_INSTANCE = new AoConsumer(aoRender);
            AO_CULL_INSTANCE = new AoConsumer(cullRender);
        } else {
            AO_INSTANCE = INSTANCE;
            AO_CULL_INSTANCE = CULL_INSTANCE;
        }
    }

    @Overwrite(remap = false)
    private static void onReloadLevelRenderer(boolean ao, BlockColors blockColors, ThreadLocalObjects objects) {
        Renderer renderer = Renderer.get();
        AltModelBlockRenderer aoRender = renderer.altModelBlockRenderer(ao, false, blockColors);
        AltModelBlockRenderer cullRender = renderer.altModelBlockRenderer(ao, true, blockColors);
        objects.instance = new Consumer(aoRender);
        objects.cullInstance = new Consumer(cullRender);
        if (ao) {
            objects.aoInstance = new AoConsumer(aoRender);
            objects.aoCullInstance = new AoConsumer(cullRender);
        } else {
            objects.aoInstance = objects.instance;
            objects.aoCullInstance = objects.cullInstance;
        }
    }

    private static class Consumer implements ModelConsumer {
        private final AltModelBlockRenderer renderer;
        protected @UnknownNullability QuadEmitter emitter;

        public Consumer(AltModelBlockRenderer renderer) {
            this.renderer = renderer;
        }

        @Override
        public void updateOutput(@NonNull BufferEmitterOutput output) {
            emitter = ((FabricEmitterSupplier) output).quadEmitter();
        }

        @Override
        public void tesselateBlock(
            float x,
            float y,
            float z,
            @NonNull BlockAndTintGetter level,
            @NonNull BlockPos pos,
            @NonNull BlockState blockState,
            @NonNull BlockStateModel model,
            long seed
        ) {
            renderer.tesselateBlock(emitter, x, y, z, level, pos, blockState, model, seed);
        }
    }

    private static class AoConsumer extends Consumer implements QuadTransform {
        private TriState defaultAo;

        public AoConsumer(AltModelBlockRenderer renderer) {
            super(renderer);
        }

        @Override
        public void updateOutput(@NonNull BufferEmitterOutput output) {
            super.updateOutput(output);
            emitter.pushTransform(this);
        }

        @Override
        public void tesselateBlock(
            float x,
            float y,
            float z,
            @NonNull BlockAndTintGetter level,
            @NonNull BlockPos pos,
            @NonNull BlockState blockState,
            @NonNull BlockStateModel model,
            long seed
        ) {
            defaultAo = TriState.of(blockState.getLightEmission() == 0);
            super.tesselateBlock(x, y, z, level, pos, blockState, model, seed);
        }

        @Override
        public boolean transform(@NonNull MutableQuadView quad) {
            if (quad.ambientOcclusion() == TriState.DEFAULT) {
                quad.ambientOcclusion(defaultAo);
            }
            return true;
        }
    }
}
