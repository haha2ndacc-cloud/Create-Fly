package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class GogglesModel implements ItemModel {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "model/goggles");
    public static final Identifier ITEM_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/goggles");
    public static final Identifier BLOCK_ID = Identifier.fromNamespaceAndPath(MOD_ID, "block/goggles");

    private final Matrix4fc transformation;
    private final List<BakedQuad> itemQuads;
    private final ModelRenderProperties itemSettings;
    private final Supplier<Vector3fc[]> itemVector;
    private final List<BakedQuad> blockQuads;
    private final ModelRenderProperties blockSettings;
    private final Supplier<Vector3fc[]> blockVector;

    public GogglesModel(
        Matrix4fc transformation,
        Tuple<List<BakedQuad>, ModelRenderProperties> item,
        Tuple<List<BakedQuad>, ModelRenderProperties> block
    ) {
        this.transformation = transformation;
        itemQuads = item.getA();
        itemSettings = item.getB();
        itemVector = Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(itemQuads));
        blockQuads = block.getA();
        blockSettings = block.getB();
        blockVector = Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(blockQuads));
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
        if (displayContext == ItemDisplayContext.HEAD) {
            update(state, displayContext, blockQuads, blockSettings, blockVector);
        } else {
            update(state, displayContext, itemQuads, itemSettings, itemVector);
        }
    }

    private void update(
        ItemStackRenderState state,
        ItemDisplayContext displayContext,
        List<BakedQuad> quads,
        ModelRenderProperties settings,
        Supplier<Vector3fc[]> vector
    ) {
        ItemStackRenderState.LayerRenderState layerRenderState = state.newLayer();
        layerRenderState.setExtents(vector);
        layerRenderState.setLocalTransform(transformation);
        settings.applyToLayer(layerRenderState, displayContext);
        layerRenderState.prepareQuadList().addAll(quads);
    }

    public static class Unbaked implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(ITEM_ID);
            resolver.markDependency(BLOCK_ID);
        }

        @Override
        public ItemModel bake(BakingContext context, Matrix4fc transformation) {
            ModelBaker baker = context.blockModelBaker();
            return new GogglesModel(transformation, bake(baker, ITEM_ID), bake(baker, BLOCK_ID));
        }

        private static Tuple<List<BakedQuad>, ModelRenderProperties> bake(ModelBaker baker, Identifier id) {
            ResolvedModel model = baker.getModel(id);
            TextureSlots textures = model.getTopTextureSlots();
            List<BakedQuad> quads = model.bakeTopGeometry(textures, baker, BlockModelRotation.IDENTITY).getAll();
            ModelRenderProperties settings = ModelRenderProperties.fromResolvedModel(baker, model, textures);
            return new Tuple<>(quads, settings);
        }
    }
}
