package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.foundation.render.CreateRenderTypes;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.SubmitNodeCollector.ParticleGroupRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.feature.ParticleFeatureRenderer.ParticleBufferCache;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState.PreparedBuffers;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.util.Util;
import org.joml.Matrix4fStack;

import java.util.*;

public class DefaultSuperRenderTypeBuffer implements SuperRenderTypeBuffer {

    private static final DefaultSuperRenderTypeBuffer INSTANCE = new DefaultSuperRenderTypeBuffer();

    public static DefaultSuperRenderTypeBuffer getInstance() {
        return INSTANCE;
    }

    protected SuperRenderTypeBufferPhase earlyBuffer;
    protected SuperRenderTypeBufferPhase defaultBuffer;
    protected SuperRenderTypeBufferPhase lateBuffer;

    public DefaultSuperRenderTypeBuffer() {
        earlyBuffer = new SuperRenderTypeBufferPhase();
        defaultBuffer = new SuperRenderTypeBufferPhase();
        lateBuffer = new SuperRenderTypeBufferPhase();
    }

    @Override
    public VertexConsumer getEarlyBuffer(RenderType type) {
        return earlyBuffer.bufferSource.getBuffer(type);
    }

    @Override
    public VertexConsumer getBuffer(RenderType type) {
        return defaultBuffer.bufferSource.getBuffer(type);
    }

    @Override
    public VertexConsumer getLateBuffer(RenderType type) {
        return lateBuffer.bufferSource.getBuffer(type);
    }

    @Override
    public void draw() {
        earlyBuffer.bufferSource.endBatch();
        defaultBuffer.bufferSource.endBatch();
        lateBuffer.bufferSource.endBatch();
    }

    @Override
    public void draw(RenderType type) {
        earlyBuffer.bufferSource.endBatch(type);
        defaultBuffer.bufferSource.endBatch(type);
        lateBuffer.bufferSource.endBatch(type);
    }

    private void drawSolid() {
        earlyBuffer.drawSolid();
        defaultBuffer.drawSolid();
        lateBuffer.drawSolid();
    }

    private void drawTranslucent() {
        earlyBuffer.drawTranslucent();
        defaultBuffer.drawTranslucent();
        lateBuffer.drawTranslucent();
    }

    public static class SuperRenderTypeBufferPhase {
        private final ArrayList<RenderType> solidTypes = new ArrayList<>();
        private final ArrayList<RenderType> translucentTypes = new ArrayList<>();
        // Visible clones from RenderBuffers
        private final SectionBufferBuilderPack fixedBufferPack = new SectionBufferBuilderPack();
        private final SortedMap<RenderType, ByteBufferBuilder> fixedBuffers = Util.make(
            new Object2ObjectLinkedOpenHashMap<>(), map -> {
                put(map, RenderTypes.solidMovingBlock());
                put(map, Sheets.cutoutBlockSheet());
                put(map, RenderTypes.cutoutMovingBlock());
                put(map, Sheets.cutoutBlockItemSheet(), fixedBufferPack.buffer(ChunkSectionLayer.CUTOUT));
                put(map, Sheets.cutoutItemSheet());
                put(map, Sheets.translucentBlockSheet());
                put(map, RenderTypes.translucentMovingBlock());
                put(
                    map,
                    Sheets.translucentBlockItemSheet(),
                    this.fixedBufferPack.buffer(ChunkSectionLayer.TRANSLUCENT)
                );
                put(map, Sheets.translucentItemSheet());
                put(map, RenderTypes.armorEntityGlint());
                put(map, RenderTypes.glint());
                put(map, RenderTypes.glintTranslucent());
                put(map, RenderTypes.entityGlint());
                put(map, RenderTypes.waterMask());
                ModelBakery.DESTROY_TYPES.forEach(renderType -> put(map, renderType));

                //extras
                put(map, PonderRenderTypes.outlineSolid());
                put(map, CreateRenderTypes.translucent());
                put(map, CreateRenderTypes.additive());
            }
        );
        private final BufferSource bufferSource = MultiBufferSource.immediateWithBuffers(
            fixedBuffers,
            new ByteBufferBuilder(256)
        );

        private void put(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> map, RenderType type) {
            put(map, type, new ByteBufferBuilder(type.bufferSize()));
        }

        private void put(
            Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> map,
            RenderType type,
            ByteBufferBuilder buffer
        ) {
            map.put(type, buffer);
            if (type.hasBlending()) {
                translucentTypes.add(type);
            } else {
                solidTypes.add(type);
            }
        }

        private void drawSolid() {
            bufferSource.endLastBatch();
            solidTypes.forEach(bufferSource::endBatch);
        }

        private void drawTranslucent() {
            drawSolid();
            translucentTypes.forEach(bufferSource::endBatch);
        }
    }

    public static class Dispatcher {
        private static final Dispatcher INSTANCE = new Dispatcher();

        public static Dispatcher getInstance() {
            return INSTANCE;
        }

        private final DefaultSuperRenderTypeBuffer buffer;
        private final OutlineBufferSource outline;
        private final FeatureRenderDispatcher renderDispatcher;
        private final Queue<ParticleBufferCache> availableBuffers = new ArrayDeque<>();
        private final List<ParticleBufferCache> usedBuffers = new ArrayList<>();

        public Dispatcher() {
            Minecraft mc = Minecraft.getInstance();
            buffer = DefaultSuperRenderTypeBuffer.getInstance();
            BufferSource bufferSource = buffer.defaultBuffer.bufferSource;
            outline = new OutlineBufferSource();
            renderDispatcher = new FeatureRenderDispatcher(
                new SubmitNodeStorage(),
                mc.getBlockRenderer(),
                bufferSource,
                mc.getAtlasManager(),
                outline,
                bufferSource,
                mc.font,
                mc.gameRenderer.getGameRenderState()
            );
        }

        public DefaultSuperRenderTypeBuffer getBuffer() {
            return buffer;
        }

        public SubmitNodeStorage getSubmitNodeStorage() {
            return renderDispatcher.getSubmitNodeStorage();
        }

        private Pair<List<ParticleRenderState>, List<ParticleRenderState>> extractParticles(PoseStack ms) {
            List<ParticleRenderState> solid = new ArrayList<>();
            List<ParticleRenderState> translucent = new ArrayList<>();
            Matrix4fStack stack = RenderSystem.getModelViewStack();
            stack.pushMatrix();
            stack.mul(ms.last().pose());
            for (SubmitNodeCollection commandQueue : renderDispatcher.getSubmitNodeStorage().getSubmitsPerOrder()
                .values()) {
                List<ParticleGroupRenderer> commands = commandQueue.getParticleGroupRenderers();
                if (commands.isEmpty()) {
                    continue;
                }
                for (ParticleGroupRenderer particleGroupRenderer : commands) {
                    ParticleBufferCache particleBufferCache = availableBuffers.poll();
                    if (particleBufferCache == null) {
                        particleBufferCache = new ParticleBufferCache();
                    }
                    usedBuffers.add(particleBufferCache);
                    PreparedBuffers preparedBuffers = particleGroupRenderer.prepare(particleBufferCache, false);
                    if (preparedBuffers != null) {
                        solid.add(new ParticleRenderState(particleGroupRenderer, preparedBuffers, particleBufferCache));
                    }
                    particleBufferCache = availableBuffers.poll();
                    if (particleBufferCache == null) {
                        particleBufferCache = new ParticleBufferCache();
                    }
                    usedBuffers.add(particleBufferCache);
                    preparedBuffers = particleGroupRenderer.prepare(particleBufferCache, true);
                    if (preparedBuffers != null) {
                        translucent.add(new ParticleRenderState(
                            particleGroupRenderer,
                            preparedBuffers,
                            particleBufferCache
                        ));
                    }
                }
                commands.clear();
            }
            stack.popMatrix();
            return Pair.of(solid, translucent);
        }

        private static void renderParticles(List<ParticleRenderState> particles) {
            if (particles.isEmpty()) {
                return;
            }
            GpuTextureView colorTexture = RenderSystem.outputColorTextureOverride;
            GpuTextureView depthTexture = RenderSystem.outputDepthTextureOverride;
            Minecraft mc = Minecraft.getInstance();
            GpuTextureView lightTextureView = mc.gameRenderer.lightmap();
            GpuSampler lightSampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
            TextureManager textureManager = mc.getTextureManager();
            GpuBufferSlice projection = RenderSystem.getProjectionMatrixBuffer();
            GpuBufferSlice fog = RenderSystem.getShaderFog();
            for (ParticleRenderState particle : particles) {
                particle.render(
                    colorTexture,
                    depthTexture,
                    projection,
                    fog,
                    lightTextureView,
                    lightSampler,
                    textureManager
                );
            }
        }

        public void draw(PoseStack ms) {
            Pair<List<ParticleRenderState>, List<ParticleRenderState>> particles = extractParticles(ms);
            renderDispatcher.renderSolidFeatures();
            buffer.drawSolid();
            renderParticles(particles.getFirst());
            outline.endOutlineBatch();
            renderParticles(particles.getSecond());
            renderDispatcher.renderTranslucentFeatures();
            buffer.drawTranslucent();
            renderDispatcher.clearSubmitNodes();
            for (ParticleBufferCache particleBufferCache : usedBuffers) {
                particleBufferCache.rotate();
            }
            availableBuffers.addAll(usedBuffers);
            usedBuffers.clear();
        }
    }

    private record ParticleRenderState(ParticleGroupRenderer particleGroupRenderer, PreparedBuffers preparedBuffers,
                                       ParticleBufferCache particleBufferCache) {
        public void render(
            GpuTextureView colorTexture,
            GpuTextureView depthTexture,
            GpuBufferSlice projection,
            GpuBufferSlice fog,
            GpuTextureView lightTextureView,
            GpuSampler lightSampler,
            TextureManager textureManager
        ) {
            try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(
                    () -> "Immediate draw for particle",
                    colorTexture,
                    OptionalInt.empty(),
                    depthTexture,
                    OptionalDouble.empty()
                )) {
                renderPass.setUniform("Projection", projection);
                renderPass.setUniform("Fog", fog);
                renderPass.bindTexture("Sampler2", lightTextureView, lightSampler);
                particleGroupRenderer.render(preparedBuffers, particleBufferCache, renderPass, textureManager);
            }
        }
    }
}
