package com.zurrtum.create.client.content.schematics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.levelWrappers.SchematicRenderLevel;
import com.zurrtum.create.client.catnip.render.EntityBlockSbbBuilder;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelRenderHelper;
import com.zurrtum.create.client.foundation.render.BlockEntityRenderHelper;
import com.zurrtum.create.client.foundation.render.BlockEntityRenderHelper.BlockEntityListRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class SchematicRenderer {

    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(
        ThreadLocalObjects::new);

    private @Nullable SuperByteBufferRenderState bufferCache;
    private boolean changed;
    protected final SchematicRenderLevel schematic;
    private final BlockPos anchor;
    private final List<BlockEntity> renderedBlockEntities = new ArrayList<>();
    private final BitSet shouldRenderBlockEntities = new BitSet();
    private final BitSet scratchErroredBlockEntities = new BitSet();

    public SchematicRenderer(SchematicRenderLevel world) {
        anchor = world.anchor;
        schematic = world;
        changed = true;

        for (var renderedBlockEntity : schematic.getRenderedBlockEntities()) {
            renderedBlockEntities.add(renderedBlockEntity);
        }
        shouldRenderBlockEntities.set(0, renderedBlockEntities.size());
    }

    public void update() {
        changed = true;
    }

    public void render(
        Minecraft mc,
        PoseStack ms,
        SubmitNodeCollector queue,
        SchematicTransformation transformation,
        CameraRenderState camera
    ) {
        if (mc.level == null || mc.player == null) {
            return;
        }
        if (changed) {
            redraw(mc);
        }
        changed = false;

        if (bufferCache != null) {
            bufferCache.submit(ms, queue);
        }
        scratchErroredBlockEntities.clear();
        BlockEntityListRenderState renderState = BlockEntityRenderHelper.getBlockEntitiesRenderState(
            VisualizationManager.supportsVisualization(schematic),
            renderedBlockEntities,
            shouldRenderBlockEntities,
            scratchErroredBlockEntities,
            null,
            schematic,
            null,
            null,
            transformation.toLocalSpace(camera.pos),
            AnimationTickHolder.getPartialTicks()
        );
        if (renderState != null) {
            renderState.submit(ms, queue, camera);
        }

        // Don't bother looping over errored BEs again.
        shouldRenderBlockEntities.andNot(scratchErroredBlockEntities);
    }

    protected void redraw(Minecraft mc) {
        BlockStateModelSet blockStateModelSet = mc.getModelManager().getBlockStateModelSet();
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

        MutableBlockPos mutableBlockPos = objects.mutableBlockPos;
        BoundingBox bounds = schematic.getBounds();

        EntityBlockSbbBuilder sbbBuilder = objects.sbbBuilder;
        schematic.renderMode = true;
        ModelConsumer renderer = ModelRenderHelper.getCullHelper(sbbBuilder);
        BlockModelLighter.enableCaching();
        for (BlockPos localPos : BlockPos.betweenClosed(
            bounds.minX(),
            bounds.minY(),
            bounds.minZ(),
            bounds.maxX(),
            bounds.maxY(),
            bounds.maxZ()
        )) {
            BlockPos pos = mutableBlockPos.setWithOffset(localPos, anchor);
            BlockState state = schematic.getBlockState(pos);
            if (state.getRenderShape() == RenderShape.MODEL) {
                renderer.tesselateBlock(
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    schematic,
                    pos,
                    state,
                    blockStateModelSet.get(state),
                    state.getSeed(pos)
                );
            }
        }
        BlockModelLighter.clearCache();
        schematic.renderMode = false;

        SuperByteBuffer buffer = sbbBuilder.build();
        bufferCache = buffer.cardinalLighting(mc.level.cardinalLighting()).keepAlive().extractRenderState();
    }

    private static class ThreadLocalObjects {
        public final MutableBlockPos mutableBlockPos = new MutableBlockPos();
        public final EntityBlockSbbBuilder sbbBuilder = new EntityBlockSbbBuilder();
    }

}
