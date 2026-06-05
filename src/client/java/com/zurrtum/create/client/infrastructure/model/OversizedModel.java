package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;
import static com.zurrtum.create.client.flywheel.lib.model.baked.ItemModelRenderHelper.submitQuads;

public class OversizedModel implements ItemModel {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "model/oversized");
    private final List<ItemTintSource> tints;
    private final QuadCollection quads;
    private final Supplier<Vector3fc[]> vector;
    private final ModelRenderProperties settings;
    private final Matrix4fc transformation;
    private final AABB box;

    public OversizedModel(
        List<ItemTintSource> tints,
        QuadCollection quads,
        ModelRenderProperties settings,
        Matrix4fc transformation,
        AABB box
    ) {
        this.tints = tints;
        this.quads = quads;
        this.settings = settings;
        this.transformation = transformation;
        vector = Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(quads.getAll()));
        this.box = box;
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
        LayerRenderState layerRenderState = submitQuads(state, settings, displayContext, quads.getAll());
        if (stack.hasFoil()) {
            layerRenderState.setFoilType(FoilType.STANDARD);
            state.setAnimated();
            state.appendModelIdentityElement(FoilType.STANDARD);
        } else if (quads.hasMaterialFlag(2)) {
            state.setAnimated();
        }
        if (!tints.isEmpty()) {
            IntList tintLayers = layerRenderState.tintLayers();

            for (ItemTintSource tintSource : tints) {
                int tint = tintSource.calculate(
                    stack,
                    world,
                    heldItemContext == null ? null : heldItemContext.asLivingEntity()
                );
                tintLayers.add(tint);
                state.appendModelIdentityElement(tint);
            }
        }
        layerRenderState.setExtents(vector);
        layerRenderState.setLocalTransform(transformation);
        if (displayContext == ItemDisplayContext.GUI) {
            state.setOversizedInGui(true);
            state.cachedModelBoundingBox = box;
        }
    }

    public record Unbaked(Identifier model, Optional<Transformation> transformation, List<ItemTintSource> tints,
                          List<Double> min, List<Double> max) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("model").forGetter(Unbaked::model),
            Transformation.EXTENDED_CODEC.optionalFieldOf("transformation").forGetter(Unbaked::transformation),
            ItemTintSources.CODEC.listOf().optionalFieldOf("tints", List.of()).forGetter(Unbaked::tints),
            Codec.DOUBLE.listOf(3, 3).fieldOf("min").forGetter(Unbaked::min),
            Codec.DOUBLE.listOf(3, 3).fieldOf("max").forGetter(Unbaked::max)
        ).apply(instance, Unbaked::new));

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(model);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel bakedSimpleModel = baker.getModel(model);
            TextureSlots modelTextures = bakedSimpleModel.getTopTextureSlots();
            QuadCollection quads = bakedSimpleModel.bakeTopGeometry(modelTextures, baker, BlockModelRotation.IDENTITY);
            ModelRenderProperties modelSettings = ModelRenderProperties.fromResolvedModel(
                baker,
                bakedSimpleModel,
                modelTextures
            );
            Matrix4fc modelTransform = Transformation.compose(transformation, this.transformation);
            return new OversizedModel(
                tints,
                quads,
                modelSettings,
                modelTransform,
                new AABB(min.get(0), min.get(1), min.get(2), max.get(0), max.get(1), max.get(2))
            );
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
