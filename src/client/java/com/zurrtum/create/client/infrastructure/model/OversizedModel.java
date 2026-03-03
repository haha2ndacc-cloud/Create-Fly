package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class OversizedModel implements ItemModel {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "model/oversized");
    private final List<ItemTintSource> tints;
    private final List<BakedQuad> quads;
    private final Supplier<Vector3fc[]> vector;
    private final ModelRenderProperties settings;
    private final AABB box;
    private final boolean animated;

    public OversizedModel(List<ItemTintSource> tints, List<BakedQuad> quads, ModelRenderProperties settings, AABB box) {
        this.tints = tints;
        this.quads = quads;
        this.settings = settings;
        this.vector = Suppliers.memoize(() -> BlockModelWrapper.computeExtents(this.quads));
        this.box = box;
        boolean animated = false;

        for (BakedQuad bakedQuad : quads) {
            if (bakedQuad.spriteInfo().sprite().contents().isAnimated()) {
                animated = true;
                break;
            }
        }

        this.animated = animated;
    }

    @Override
    public void update(
        ItemStackRenderState state,
        ItemStack stack,
        ItemModelResolver resolver,
        ItemDisplayContext displayContext,
        @Nullable ClientLevel world,
        @Nullable ItemOwner heldItemContext,
        int seed
    ) {
        state.appendModelIdentityElement(this);
        ItemStackRenderState.LayerRenderState layerRenderState = state.newLayer();
        if (stack.hasFoil()) {
            layerRenderState.setFoilType(ItemStackRenderState.FoilType.STANDARD);
            state.setAnimated();
            state.appendModelIdentityElement(ItemStackRenderState.FoilType.STANDARD);
        }

        int i = tints.size();
        int[] is = layerRenderState.prepareTintLayers(i);

        for (int j = 0; j < i; j++) {
            int k = tints.get(j)
                .calculate(stack, world, heldItemContext == null ? null : heldItemContext.asLivingEntity());
            is[j] = k;
            state.appendModelIdentityElement(k);
        }

        layerRenderState.setExtents(vector);
        settings.applyToLayer(layerRenderState, displayContext);
        layerRenderState.prepareQuadList().addAll(quads);
        if (animated) {
            state.setAnimated();
        }
        if (displayContext == ItemDisplayContext.GUI) {
            state.setOversizedInGui(true);
            state.cachedModelBoundingBox = box;
        }
    }

    public record Unbaked(Identifier model, List<ItemTintSource> tints, List<Double> min,
                          List<Double> max) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("model").forGetter(Unbaked::model),
            ItemTintSources.CODEC.listOf().optionalFieldOf("tints", List.of()).forGetter(Unbaked::tints),
            Codec.DOUBLE.listOf(3, 3).fieldOf("min").forGetter(Unbaked::min),
            Codec.DOUBLE.listOf(3, 3).fieldOf("max").forGetter(Unbaked::max)
        ).apply(instance, Unbaked::new));

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.model);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel bakedSimpleModel = baker.getModel(this.model);
            TextureSlots modelTextures = bakedSimpleModel.getTopTextureSlots();
            List<BakedQuad> quads = bakedSimpleModel.bakeTopGeometry(modelTextures, baker, BlockModelRotation.IDENTITY)
                .getAll();
            ModelRenderProperties modelSettings = ModelRenderProperties.fromResolvedModel(
                baker,
                bakedSimpleModel,
                modelTextures
            );
            return new OversizedModel(
                tints,
                quads,
                modelSettings,
                new AABB(min.get(0), min.get(1), min.get(2), max.get(0), max.get(1), max.get(2))
            );
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
