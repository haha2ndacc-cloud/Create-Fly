package com.zurrtum.create.client.flywheel.lib.model.baked;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.UnknownNullability;

public class ModelRenderHelper {
    private static @UnknownNullability ModelConsumer INSTANCE;
    private static @UnknownNullability ModelConsumer CULL_INSTANCE;

    public static ModelConsumer getCullHelper(BufferEmitterOutput output) {
        CULL_INSTANCE.updateOutput(output);
        return CULL_INSTANCE;
    }

    public static ModelConsumer getHelper(BufferEmitterOutput output) {
        INSTANCE.updateOutput(output);
        return INSTANCE;
    }

    public static void onReloadLevelRenderer() {
        Minecraft mc = Minecraft.getInstance();
        boolean ao = mc.options.ambientOcclusion().get();
        BlockColors blockColors = mc.getBlockColors();
        INSTANCE = new Consumer(ao, false, blockColors);
        CULL_INSTANCE = new Consumer(ao, true, blockColors);
    }

    private static class Consumer implements ModelConsumer {
        private final ModelBlockRenderer renderer;
        private @UnknownNullability BufferEmitterOutput output;

        public Consumer(boolean ambientOcclusion, boolean cull, BlockColors blockColors) {
            renderer = new ModelBlockRenderer(ambientOcclusion, cull, blockColors);
        }

        @Override
        public void updateOutput(BufferEmitterOutput output) {
            this.output = output;
        }

        @Override
        public void tesselateBlock(
            float x,
            float y,
            float z,
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState blockState,
            BlockStateModel model,
            long seed
        ) {
            renderer.tesselateBlock(output, x, y, z, level, pos, blockState, model, seed);
        }
    }
}
