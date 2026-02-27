package com.zurrtum.create.client.content.schematics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.levelWrappers.SchematicRenderLevel;
import com.zurrtum.create.client.catnip.render.ShadedBlockSbbBuilder;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.render.BlockEntityRenderHelper;
import com.zurrtum.create.client.foundation.render.BlockEntityRenderHelper.BlockEntityListRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class SchematicRenderer {

    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(
        ThreadLocalObjects::new);

    private final Map<ChunkSectionLayer, SuperByteBuffer> bufferCache = new LinkedHashMap<>(ChunkSectionLayer.values().length);
    private boolean changed;
    protected final SchematicRenderLevel schematic;
    private final BlockPos anchor;
    private final List<BlockEntity> renderedBlockEntities = new ArrayList<>();
    private final BitSet shouldRenderBlockEntities = new BitSet();
    private final BitSet scratchErroredBlockEntities = new BitSet();

    public SchematicRenderer(SchematicRenderLevel world) {
        this.anchor = world.anchor;
        this.schematic = world;
        this.changed = true;

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
        SuperRenderTypeBuffer buffers,
        SchematicTransformation transformation,
        Vec3 camera
    ) {
        if (mc.level == null || mc.player == null) {
            return;
        }
        if (changed) {
            redraw(mc);
        }
        changed = false;

        bufferCache.forEach((layer, buffer) -> {
            buffer.renderInto(
                ms.last(), buffers.getBuffer(switch (layer) {
                    case SOLID -> RenderTypes.solidMovingBlock();
                    case CUTOUT -> RenderTypes.cutoutMovingBlock();
                    case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
                })
            );
        });
        scratchErroredBlockEntities.clear();
        BlockEntityListRenderState renderState = BlockEntityRenderHelper.getBlockEntitiesRenderState(
            VisualizationManager.supportsVisualization(schematic),
            renderedBlockEntities,
            shouldRenderBlockEntities,
            scratchErroredBlockEntities,
            null,
            schematic,
            null,
            transformation.toLocalSpace(camera),
            AnimationTickHolder.getPartialTicks()
        );
        if (renderState != null) {
            FeatureRenderDispatcher renderDispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
            renderState.render(
                ms,
                renderDispatcher.getSubmitNodeStorage(),
                mc.gameRenderer.getLevelRenderState().cameraRenderState
            );
        }

        // Don't bother looping over errored BEs again.
        shouldRenderBlockEntities.andNot(scratchErroredBlockEntities);
    }

    protected void redraw(Minecraft mc) {
        bufferCache.clear();

        for (ChunkSectionLayer layer : ChunkSectionLayer.values()) {
            SuperByteBuffer buffer = drawLayer(mc, layer);
            if (!buffer.isEmpty()) {
                bufferCache.put(layer, buffer);
            }
        }
    }

    @SuppressWarnings("removal")
    protected SuperByteBuffer drawLayer(Minecraft mc, ChunkSectionLayer layer) {
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

        BlockPos.MutableBlockPos mutableBlockPos = objects.mutableBlockPos;
        BoundingBox bounds = schematic.getBounds();

        ShadedBlockSbbBuilder sbbBuilder = objects.sbbBuilder;
        sbbBuilder.begin(layer);

        schematic.renderMode = true;
        boolean ambientOcclusion = mc.options.ambientOcclusion().get();
        ModelBlockRenderer renderer = new ModelBlockRenderer(ambientOcclusion, true, mc.getBlockColors());
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
                    sbbBuilder,
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    schematic,
                    pos,
                    state,
                    dispatcher.getBlockModel(state),
                    state.getSeed(pos)
                );
            }
        }
        BlockModelLighter.clearCache();
        schematic.renderMode = false;

        return sbbBuilder.end();
    }

    @SuppressWarnings("removal")
    private static class ThreadLocalObjects {
        public final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        public final ShadedBlockSbbBuilder sbbBuilder = new ShadedBlockSbbBuilder(new PoseStack());
    }

}
