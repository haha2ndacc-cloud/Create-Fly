package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import com.zurrtum.create.client.foundation.render.CreateRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class WorldshaperModel implements ItemModel, SpecialModelRenderer<WorldshaperModel.RenderData> {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "model/handheld_worldshaper");
    public static final Identifier ITEM_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/handheld_worldshaper/item");
    public static final Identifier CORE_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/handheld_worldshaper/core");
    public static final Identifier CORE_GLOW_ID = Identifier.fromNamespaceAndPath(
        MOD_ID,
        "item/handheld_worldshaper/core_glow"
    );
    public static final Identifier ACCELERATOR_ID = Identifier.fromNamespaceAndPath(
        MOD_ID,
        "item/handheld_worldshaper/accelerator"
    );
    public static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();
    private static final int[] TINTS = new int[]{-1};
    private static final int[][] LIGHT_TINTS = new int[][]{
        {0xff313138},
        {0xff3d3d42},
        {0xff4b494b},
        {0xff585451},
        {0xff665f57},
        {0xff7a7063},
        {0xff8e8070},
        {0xffa1917c},
        {0xffb3a18a},
        {0xffc5b299},
        {0xffd7c3ab},
        {0xffebd7c1},
        {0xfffff3e1},
        {0xffffffff},
        {0xffffffff},
        {0xffffffff}
    };

    private final ModelRenderProperties settings;
    private final Matrix4fc transformation;
    private final List<BakedQuad> item;
    private final List<BakedQuad> core;
    private final List<BakedQuad> coreGlow;
    private final List<BakedQuad> accelerator;
    private final Supplier<Vector3fc[]> vector;

    public WorldshaperModel(
        ModelRenderProperties settings,
        Matrix4fc transformation,
        List<BakedQuad> item,
        List<BakedQuad> core,
        List<BakedQuad> coreGlow,
        List<BakedQuad> accelerator
    ) {
        this.settings = settings;
        this.transformation = transformation;
        this.item = item;
        this.core = core;
        this.coreGlow = coreGlow;
        this.accelerator = accelerator;
        this.vector = Suppliers.memoize(() -> {
            Set<Vector3fc> set = new HashSet<>();
            addPosition(set, item);
            addPosition(set, core);
            addPosition(set, coreGlow);
            addPosition(set, accelerator);
            return set.toArray(Vector3fc[]::new);
        });
    }

    private static void addPosition(Set<Vector3fc> set, List<BakedQuad> quads) {
        for (BakedQuad bakedQuad : quads) {
            set.add(bakedQuad.position0());
            set.add(bakedQuad.position1());
            set.add(bakedQuad.position2());
            set.add(bakedQuad.position3());
        }
    }

    @Override
    public void update(
        ItemStackRenderState state,
        ItemStack stack,
        ItemModelResolver resolver,
        ItemDisplayContext displayContext,
        @Nullable ClientLevel world,
        @Nullable ItemOwner user,
        int seed
    ) {
        state.appendModelIdentityElement(this);
        state.setAnimated();
        ItemStackRenderState.LayerRenderState renderState = state.newLayer();
        renderState.setExtents(vector);
        renderState.setLocalTransform(transformation);
        renderState.setUsesBlockLight(settings.usesBlockLight());
        renderState.setParticleMaterial(settings.particleMaterial());
        RenderData data = new RenderData();
        data.transform = settings.transforms().getTransform(displayContext);
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        boolean mainHand = player.getMainHandItem() == stack;
        data.rightHand = mainHand ^ (player.getMainArm() == HumanoidArm.LEFT);
        data.inHand = mainHand || player.getOffhandItem() == stack;
        if (displayContext == ItemDisplayContext.GUI) {
            data.state = stack.get(AllDataComponents.SHAPER_BLOCK_USED);
            data.used = UsedRenderState.create(mc, data.state, displayContext, world, user, seed);
        }
        state.appendModelIdentityElement(data);
        renderState.setupSpecialModel(this, data);
    }

    @Override
    public void submit(
        @Nullable RenderData data,
        ItemDisplayContext displayContext,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay,
        boolean glint,
        int i
    ) {
        assert data != null;
        matrices.pushPose();
        matrices.translate(0.5F, 0.5F, 0.5F);
        matrices.pushPose();
        data.transform.apply(displayContext.leftHand(), matrices.last());
        renderItem(displayContext, matrices, queue, light, overlay, TINTS, item);

        float pt = AnimationTickHolder.getPartialTicks();
        float worldTime = AnimationTickHolder.getRenderTime() / 20;
        float animation = Mth.clamp(Create.ZAPPER_RENDER_HANDLER.getAnimation(data.rightHand, pt) * 5, 0, 1);

        // Core glows
        float multiplier;
        if (data.inHand) {
            multiplier = animation;
        } else {
            multiplier = Mth.sin(worldTime * 5);
        }
        int lightItensity = (int) (15 * Mth.clamp(multiplier, 0, 1));
        if (displayContext == ItemDisplayContext.GUI) {
            int[] glowTint = LIGHT_TINTS[lightItensity];
            renderItem(displayContext, matrices, queue, 0, overlay, glowTint, core);
            renderItem(displayContext, matrices, queue, 0, overlay, glowTint, coreGlow);
        } else {
            int glowLight = LightCoordsUtil.pack(lightItensity, Math.max(lightItensity, 4));
            renderItem(displayContext, matrices, queue, glowLight, overlay, TINTS, core);
            renderItem(displayContext, matrices, queue, glowLight, overlay, TINTS, coreGlow);
        }

        // Accelerator spins
        float angle = worldTime * -25;
        if (data.inHand) {
            angle += 360 * animation;
        }

        angle %= 360;
        matrices.translate(0.5f, 0.345f, 0.5f);
        matrices.mulPose(Axis.ZP.rotationDegrees(angle));
        matrices.translate(-0.5f, -0.345f, -0.5f);
        renderItem(displayContext, matrices, queue, light, overlay, TINTS, accelerator);
        matrices.popPose();

        if (data.used != null) {
            data.used.render(matrices, queue, light, overlay);
        }
        matrices.popPose();
    }

    private static void renderItem(
        ItemDisplayContext displayContext,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay,
        int[] tintLayers,
        List<BakedQuad> item
    ) {
        queue.submitItem(
            matrices,
            displayContext,
            light,
            overlay,
            0,
            tintLayers,
            item,
            ItemStackRenderState.FoilType.NONE
        );
    }

    public static class RenderData {
        public ItemTransform transform;
        public @Nullable BlockState state;
        public boolean rightHand;
        public boolean inHand;
        public @Nullable UsedRenderState used;

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof RenderData data)) {
                return false;
            }
            return transform == data.transform && state == data.state && rightHand == data.rightHand && inHand == data.inHand;
        }

        @Override
        public int hashCode() {
            return Objects.hash(rightHand, inHand);
        }
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RenderData extractArgument(ItemStack stack) {
        throw new UnsupportedOperationException();
    }

    public static class Unbaked implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(ITEM_ID);
            resolver.markDependency(CORE_ID);
            resolver.markDependency(CORE_GLOW_ID);
            resolver.markDependency(ACCELERATOR_ID);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel model = baker.getModel(ITEM_ID);
            TextureSlots textures = model.getTopTextureSlots();
            List<BakedQuad> quads = model.bakeTopGeometry(textures, baker, BlockModelRotation.IDENTITY).getAll();
            ModelRenderProperties settings = ModelRenderProperties.fromResolvedModel(baker, model, textures);
            List<BakedQuad> core = BakedModelHelper.replaceQuadLayer(
                BakedModelHelper.bakeQuads(baker, CORE_ID),
                ChunkSectionLayer.SOLID,
                CreateRenderTypes.itemGlowingSolid()
            );
            List<BakedQuad> coreGlow = BakedModelHelper.replaceQuadLayer(
                BakedModelHelper.bakeQuads(
                    baker,
                    CORE_GLOW_ID
                ),
                ChunkSectionLayer.TRANSLUCENT,
                CreateRenderTypes.itemGlowingTranslucent()
            );
            return new WorldshaperModel(
                settings,
                transformation,
                quads,
                core,
                coreGlow,
                BakedModelHelper.bakeQuads(baker, ACCELERATOR_ID)
            );
        }
    }

    public interface UsedRenderState {
        @Nullable
        static UsedRenderState create(
            Minecraft mc,
            @Nullable BlockState state,
            ItemDisplayContext displayContext,
            @Nullable ClientLevel world,
            @Nullable ItemOwner user,
            int seed
        ) {
            if (state == null) {
                return null;
            }
            if (state.getBlock() instanceof CrossCollisionBlock block) {
                return UsedItemRenderState.create(mc, block, displayContext, world, user, seed);
            }
            BlockModelRenderState model = new BlockModelRenderState();
            mc.blockModelResolver.update(model, state, BLOCK_DISPLAY_CONTEXT);
            return new UsedBlockRenderState(model);
        }

        void render(PoseStack matrices, SubmitNodeCollector queue, int light, int overlay);
    }

    public record UsedItemRenderState(@Nullable Lighting diffuseLighting, @Nullable BufferSource entityVertexConsumers,
                                      @Nullable FeatureRenderDispatcher entityRenderDispatcher,
                                      ItemStackRenderState state) implements UsedRenderState {
        public static UsedItemRenderState create(
            Minecraft mc,
            CrossCollisionBlock block,
            ItemDisplayContext displayContext,
            @Nullable ClientLevel world,
            @Nullable ItemOwner user,
            int seed
        ) {
            ItemStackRenderState item = new ItemStackRenderState();
            item.displayContext = displayContext;
            mc.getItemModelResolver()
                .appendItemLayers(item, block.asItem().getDefaultInstance(), displayContext, world, user, seed);
            if (item.usesBlockLight()) {
                return new UsedItemRenderState(null, null, null, item);
            }
            GameRenderer gameRenderer = mc.gameRenderer;
            return new UsedItemRenderState(
                gameRenderer.getLighting(),
                mc.renderBuffers().bufferSource(),
                gameRenderer.getFeatureRenderDispatcher(),
                item
            );
        }

        public void render(PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
            if (diffuseLighting != null) {
                entityRenderDispatcher.renderAllFeatures();
                entityVertexConsumers.endBatch();
                diffuseLighting.setupFor(Lighting.Entry.ITEMS_FLAT);
            }
            matrices.translate(-0.242f, -0.278f, 0);
            matrices.scale(0.25f, 0.25f, 0.25f);
            matrices.mulPose(Axis.XP.rotationDegrees(30));
            matrices.mulPose(Axis.YP.rotationDegrees(45));
            state.submit(matrices, queue, light, overlay, 0);
        }
    }

    public record UsedBlockRenderState(BlockModelRenderState model) implements UsedRenderState {
        public void render(PoseStack matrices, SubmitNodeCollector queue, int light, int overlay) {
            matrices.translate(-0.42f, -0.385f, 0);
            matrices.scale(0.25f, 0.25f, 0.25f);
            matrices.mulPose(Axis.XP.rotationDegrees(30));
            matrices.mulPose(Axis.YP.rotationDegrees(45));
            model.submit(matrices, queue, light, overlay, 0);
        }
    }
}
