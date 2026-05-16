package com.zurrtum.create.client;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.infrastructure.model.*;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel.UnbakedRoot;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ColorCollection;
import net.minecraft.world.level.block.WeatheringCopperCollection;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class AllModels {
    public static final Map<Block, BiFunction<BlockState, UnbakedRoot, UnbakedRoot>> ALL = new HashMap<>();

    public static void register(Block block, BiFunction<BlockState, UnbakedRoot, UnbakedRoot> resolver) {
        ALL.put(block, resolver);
    }

    private static void register(
        WeatheringCopperCollection<? extends Block> states,
        BiFunction<BlockState, UnbakedRoot, UnbakedRoot> unaffected,
        BiFunction<BlockState, UnbakedRoot, UnbakedRoot> exposed,
        BiFunction<BlockState, UnbakedRoot, UnbakedRoot> weathered,
        BiFunction<BlockState, UnbakedRoot, UnbakedRoot> oxidized
    ) {
        WeatheringCopperCollection.ByState<? extends Block> weathering = states.weathering();
        ALL.put(weathering.unaffected(), unaffected);
        ALL.put(weathering.exposed(), exposed);
        ALL.put(weathering.weathered(), weathered);
        ALL.put(weathering.oxidized(), oxidized);
        WeatheringCopperCollection.ByState<? extends Block> waxed = states.waxed();
        ALL.put(waxed.unaffected(), unaffected);
        ALL.put(waxed.exposed(), exposed);
        ALL.put(waxed.weathered(), weathered);
        ALL.put(waxed.oxidized(), oxidized);
    }

    public static void register(
        ColorCollection<? extends Block> colors,
        BiFunction<BlockState, UnbakedRoot, UnbakedRoot> resolver
    ) {
        ColorCollection.zipApply(colors, ColorCollection.create(resolver), ALL::put);
    }

    public static <T extends ItemModel.Unbaked> void register(Identifier id, MapCodec<T> codec) {
        ItemModels.ID_MAPPER.put(id, codec);
    }

    public static void register() {
        register(WrenchModel.ID, WrenchModel.Unbaked.CODEC);
        register(PotatoCannonModel.ID, PotatoCannonModel.Unbaked.CODEC);
        register(ExtendoGripModel.ID, ExtendoGripModel.Unbaked.CODEC);
        register(SymmetryWandModel.ID, SymmetryWandModel.Unbaked.CODEC);
        register(WorldshaperModel.ID, WorldshaperModel.Unbaked.CODEC);
        register(OversizedModel.ID, OversizedModel.Unbaked.CODEC);
        register(NormalModel.ID, NormalModel.Unbaked.CODEC);
        register(SandPaperModel.ID, SandPaperModel.Unbaked.CODEC);
        register(LinkedControllerModel.ID, LinkedControllerModel.Unbaked.CODEC);

        BiFunction<BlockState, UnbakedRoot, UnbakedRoot> andesiteCasing = CTModel.of(AllCTBehaviours.ANDESITE_CASING);
        BiFunction<BlockState, UnbakedRoot, UnbakedRoot> brassCasing = CTModel.of(AllCTBehaviours.BRASS_CASING);
        BiFunction<BlockState, UnbakedRoot, UnbakedRoot> copperCasing = CTModel.of(AllCTBehaviours.COPPER_CASING);
        BiFunction<BlockState, UnbakedRoot, UnbakedRoot> chassis = CTModel.of(AllCTBehaviours.CHASSIS);
        BiFunction<BlockState, UnbakedRoot, UnbakedRoot> copperShingles = CTModel.of(AllCTBehaviours.COPPER_SHINGLES);
        BiFunction<BlockState, UnbakedRoot, UnbakedRoot> exposedCopperShingles = CTModel.of(AllCTBehaviours.EXPOSED_COPPER_SHINGLES);
        BiFunction<BlockState, UnbakedRoot, UnbakedRoot> weatheredCopperShingles = CTModel.of(AllCTBehaviours.WEATHERED_COPPER_SHINGLES);
        BiFunction<BlockState, UnbakedRoot, UnbakedRoot> oxidizedCopperShingles = CTModel.of(AllCTBehaviours.OXIDIZED_COPPER_SHINGLES);
        BiFunction<BlockState, UnbakedRoot, UnbakedRoot> copperTiles = CTModel.of(AllCTBehaviours.COPPER_TILES);
        BiFunction<BlockState, UnbakedRoot, UnbakedRoot> exposedcopperTiles = CTModel.of(AllCTBehaviours.EXPOSED_COPPER_TILES);
        BiFunction<BlockState, UnbakedRoot, UnbakedRoot> weatheredcopperTiles = CTModel.of(AllCTBehaviours.WEATHERED_COPPER_TILES);
        BiFunction<BlockState, UnbakedRoot, UnbakedRoot> oxidizedcopperTiles = CTModel.of(AllCTBehaviours.OXIDIZED_COPPER_TILES);
        register(AllBlocks.ANDESITE_CASING, andesiteCasing);
        register(AllBlocks.GEARBOX, andesiteCasing);
        register(AllBlocks.ANDESITE_ENCASED_SHAFT, andesiteCasing);
        register(AllBlocks.BRASS_CASING, brassCasing);
        register(AllBlocks.BRASS_ENCASED_SHAFT, brassCasing);
        register(AllBlocks.COPPER_CASING, copperCasing);
        register(AllBlocks.RAILWAY_CASING, CTModel.of(AllCTBehaviours.RAILWAY_CASING));
        register(AllBlocks.SHADOW_STEEL_CASING, CTModel.of(AllCTBehaviours.SHADOW_STEEL_CASING));
        register(AllBlocks.REFINED_RADIANCE_CASING, CTModel.of(AllCTBehaviours.REFINED_RADIANCE_CASING));
        register(AllBlocks.ANDESITE_ENCASED_COGWHEEL, CTModel.of(AllCTBehaviours.COG_SIDE_ANDESITE_CASING));
        register(AllBlocks.BRASS_ENCASED_COGWHEEL, CTModel.of(AllCTBehaviours.COG_SIDE_BRASS_CASING));
        register(AllBlocks.ANDESITE_ENCASED_LARGE_COGWHEEL, CTModel.of(AllCTBehaviours.COG_ANDESITE_CASING));
        register(AllBlocks.BRASS_ENCASED_LARGE_COGWHEEL, CTModel.of(AllCTBehaviours.COG_BRASS_CASING));
        register(AllBlocks.LINEAR_CHASSIS, chassis);
        register(AllBlocks.SECONDARY_LINEAR_CHASSIS, chassis);
        register(AllBlocks.ENCASED_FLUID_PIPE, copperCasing);
        register(AllBlocks.INDUSTRIAL_IRON_WINDOW, CTModel.of(AllCTBehaviours.INDUSTRIAL_IRON_WINDOW));
        register(AllBlocks.INDUSTRIAL_IRON_WINDOW_PANE, CTModel.of(AllCTBehaviours.INDUSTRIAL_IRON_WINDOW_PANE));
        register(AllBlocks.WEATHERED_IRON_WINDOW, CTModel.of(AllCTBehaviours.WEATHERED_IRON_WINDOW));
        register(AllBlocks.WEATHERED_IRON_WINDOW_PANE, CTModel.of(AllCTBehaviours.WEATHERED_IRON_WINDOW_PANE));
        register(AllBlocks.BRASS_TUNNEL, CTModel.of(AllCTBehaviours.BRASS_TUNNEL));
        register(AllBlocks.MECHANICAL_CRAFTER, CTModel.of(AllCTBehaviours.CRAFTER));
        register(AllBlocks.ITEM_VAULT, CTModel.of(AllCTBehaviours.ITEM_VAULT));
        register(AllBlocks.ORNATE_IRON_WINDOW, CTModel.of(AllCTBehaviours.ORNATE_IRON_WINDOW));
        register(AllBlocks.ANDESITE_SCAFFOLD, CTModel.of(AllCTBehaviours.ANDESITE_SCAFFOLD));
        register(AllBlocks.BRASS_SCAFFOLD, CTModel.of(AllCTBehaviours.BRASS_SCAFFOLD));
        register(AllBlocks.COPPER_SCAFFOLD, CTModel.of(AllCTBehaviours.COPPER_SCAFFOLD));
        register(AllBlocks.FRAMED_GLASS_TRAPDOOR, CTModel.of(AllCTBehaviours.FRAMED_GLASS_TRAPDOOR));
        register(
            AllBlocks.COPPER_SHINGLES,
            copperShingles,
            exposedCopperShingles,
            weatheredCopperShingles,
            oxidizedCopperShingles
        );
        register(
            AllBlocks.COPPER_SHINGLE_SLAB,
            copperShingles,
            exposedCopperShingles,
            weatheredCopperShingles,
            oxidizedCopperShingles
        );
        register(
            AllBlocks.COPPER_SHINGLE_STAIRS,
            copperShingles,
            exposedCopperShingles,
            weatheredCopperShingles,
            oxidizedCopperShingles
        );
        register(AllBlocks.COPPER_TILES, copperTiles, exposedcopperTiles, weatheredcopperTiles, oxidizedcopperTiles);
        register(
            AllBlocks.COPPER_TILE_SLAB,
            copperTiles,
            exposedcopperTiles,
            weatheredcopperTiles,
            oxidizedcopperTiles
        );
        register(
            AllBlocks.COPPER_TILE_STAIRS,
            copperTiles,
            exposedcopperTiles,
            weatheredcopperTiles,
            oxidizedcopperTiles
        );
        register(AllBlocks.FRAMED_GLASS, CTModel.of(AllCTBehaviours.FRAMED_GLASS));
        register(AllBlocks.HORIZONTAL_FRAMED_GLASS, CTModel.of(AllCTBehaviours.HORIZONTAL_FRAMED_GLASS));
        register(AllBlocks.VERTICAL_FRAMED_GLASS, CTModel.of(AllCTBehaviours.VERTICAL_FRAMED_GLASS));
        register(AllBlocks.FRAMED_GLASS_PANE, CTModel.of(AllCTBehaviours.FRAMED_GLASS_PANE));
        register(AllBlocks.HORIZONTAL_FRAMED_GLASS_PANE, CTModel.of(AllCTBehaviours.HORIZONTAL_FRAMED_GLASS_PANE));
        register(AllBlocks.VERTICAL_FRAMED_GLASS_PANE, CTModel.of(AllCTBehaviours.VERTICAL_FRAMED_GLASS_PANE));
        register(AllBlocks.OAK_WINDOW, CTModel.of(AllCTBehaviours.OAK_WINDOW));
        register(AllBlocks.SPRUCE_WINDOW, CTModel.of(AllCTBehaviours.SPRUCE_WINDOW));
        register(AllBlocks.BIRCH_WINDOW, CTModel.of(AllCTBehaviours.BIRCH_WINDOW));
        register(AllBlocks.JUNGLE_WINDOW, CTModel.of(AllCTBehaviours.JUNGLE_WINDOW));
        register(AllBlocks.ACACIA_WINDOW, CTModel.of(AllCTBehaviours.ACACIA_WINDOW));
        register(AllBlocks.DARK_OAK_WINDOW, CTModel.of(AllCTBehaviours.DARK_OAK_WINDOW));
        register(AllBlocks.MANGROVE_WINDOW, CTModel.of(AllCTBehaviours.MANGROVE_WINDOW));
        register(AllBlocks.CRIMSON_WINDOW, CTModel.of(AllCTBehaviours.CRIMSON_WINDOW));
        register(AllBlocks.WARPED_WINDOW, CTModel.of(AllCTBehaviours.WARPED_WINDOW));
        register(AllBlocks.CHERRY_WINDOW, CTModel.of(AllCTBehaviours.CHERRY_WINDOW));
        register(AllBlocks.BAMBOO_WINDOW, CTModel.of(AllCTBehaviours.BAMBOO_WINDOW));
        register(AllBlocks.OAK_WINDOW_PANE, CTModel.of(AllCTBehaviours.OAK_WINDOW_PANE));
        register(AllBlocks.SPRUCE_WINDOW_PANE, CTModel.of(AllCTBehaviours.SPRUCE_WINDOW_PANE));
        register(AllBlocks.BIRCH_WINDOW_PANE, CTModel.of(AllCTBehaviours.BIRCH_WINDOW_PANE));
        register(AllBlocks.JUNGLE_WINDOW_PANE, CTModel.of(AllCTBehaviours.JUNGLE_WINDOW_PANE));
        register(AllBlocks.ACACIA_WINDOW_PANE, CTModel.of(AllCTBehaviours.ACACIA_WINDOW_PANE));
        register(AllBlocks.DARK_OAK_WINDOW_PANE, CTModel.of(AllCTBehaviours.DARK_OAK_WINDOW_PANE));
        register(AllBlocks.MANGROVE_WINDOW_PANE, CTModel.of(AllCTBehaviours.MANGROVE_WINDOW_PANE));
        register(AllBlocks.CRIMSON_WINDOW_PANE, CTModel.of(AllCTBehaviours.CRIMSON_WINDOW_PANE));
        register(AllBlocks.WARPED_WINDOW_PANE, CTModel.of(AllCTBehaviours.WARPED_WINDOW_PANE));
        register(AllBlocks.CHERRY_WINDOW_PANE, CTModel.of(AllCTBehaviours.CHERRY_WINDOW_PANE));
        register(AllBlocks.BAMBOO_WINDOW_PANE, CTModel.of(AllCTBehaviours.BAMBOO_WINDOW_PANE));
        register(AllBlocks.ORNATE_IRON_WINDOW_PANE, CTModel.of(AllCTBehaviours.ORNATE_IRON_WINDOW_PANE));
        register(AllBlocks.LAYERED_GRANITE, CTModel.of(AllCTBehaviours.LAYERED_GRANITE));
        register(AllBlocks.GRANITE_PILLAR, CTModel.of(AllCTBehaviours.GRANITE_PILLAR));
        register(AllBlocks.LAYERED_DIORITE, CTModel.of(AllCTBehaviours.LAYERED_DIORITE));
        register(AllBlocks.DIORITE_PILLAR, CTModel.of(AllCTBehaviours.DIORITE_PILLAR));
        register(AllBlocks.LAYERED_ANDESITE, CTModel.of(AllCTBehaviours.LAYERED_ANDESITE));
        register(AllBlocks.ANDESITE_PILLAR, CTModel.of(AllCTBehaviours.ANDESITE_PILLAR));
        register(AllBlocks.LAYERED_CALCITE, CTModel.of(AllCTBehaviours.LAYERED_CALCITE));
        register(AllBlocks.CALCITE_PILLAR, CTModel.of(AllCTBehaviours.CALCITE_PILLAR));
        register(AllBlocks.LAYERED_DRIPSTONE, CTModel.of(AllCTBehaviours.LAYERED_DRIPSTONE));
        register(AllBlocks.DRIPSTONE_PILLAR, CTModel.of(AllCTBehaviours.DRIPSTONE_PILLAR));
        register(AllBlocks.LAYERED_DEEPSLATE, CTModel.of(AllCTBehaviours.LAYERED_DEEPSLATE));
        register(AllBlocks.DEEPSLATE_PILLAR, CTModel.of(AllCTBehaviours.DEEPSLATE_PILLAR));
        register(AllBlocks.LAYERED_TUFF, CTModel.of(AllCTBehaviours.LAYERED_TUFF));
        register(AllBlocks.TUFF_PILLAR, CTModel.of(AllCTBehaviours.TUFF_PILLAR));
        register(AllBlocks.LAYERED_ASURINE, CTModel.of(AllCTBehaviours.LAYERED_ASURINE));
        register(AllBlocks.ASURINE_PILLAR, CTModel.of(AllCTBehaviours.ASURINE_PILLAR));
        register(AllBlocks.LAYERED_CRIMSITE, CTModel.of(AllCTBehaviours.LAYERED_CRIMSITE));
        register(AllBlocks.CRIMSITE_PILLAR, CTModel.of(AllCTBehaviours.CRIMSITE_PILLAR));
        register(AllBlocks.LAYERED_LIMESTONE, CTModel.of(AllCTBehaviours.LAYERED_LIMESTONE));
        register(AllBlocks.LIMESTONE_PILLAR, CTModel.of(AllCTBehaviours.LIMESTONE_PILLAR));
        register(AllBlocks.LAYERED_OCHRUM, CTModel.of(AllCTBehaviours.LAYERED_OCHRUM));
        register(AllBlocks.OCHRUM_PILLAR, CTModel.of(AllCTBehaviours.OCHRUM_PILLAR));
        register(AllBlocks.LAYERED_SCORIA, CTModel.of(AllCTBehaviours.LAYERED_SCORIA));
        register(AllBlocks.SCORIA_PILLAR, CTModel.of(AllCTBehaviours.SCORIA_PILLAR));
        register(AllBlocks.LAYERED_SCORCHIA, CTModel.of(AllCTBehaviours.LAYERED_SCORCHIA));
        register(AllBlocks.SCORCHIA_PILLAR, CTModel.of(AllCTBehaviours.SCORCHIA_PILLAR));
        register(AllBlocks.LAYERED_VERIDIUM, CTModel.of(AllCTBehaviours.LAYERED_VERIDIUM));
        register(AllBlocks.VERIDIUM_PILLAR, CTModel.of(AllCTBehaviours.VERIDIUM_PILLAR));

        register(AllBlocks.SHAFT, BracketedKineticBlockModel::new);
        register(AllBlocks.COGWHEEL, BracketedKineticBlockModel::new);
        register(AllBlocks.LARGE_COGWHEEL, BracketedKineticBlockModel::new);
        register(AllBlocks.GANTRY_SHAFT, GantryShaftModel::new);
        register(AllBlocks.BELT, BeltModel::new);
        register(AllBlocks.LARGE_WATER_WHEEL, LargeWaterWheelModel::new);
        register(AllBlocks.WATER_WHEEL_STRUCTURAL, WaterWheelStructuralModel::single);
        register(AllBlocks.FLUID_PIPE, PipeAttachmentModel::new);
        register(AllBlocks.ENCASED_FLUID_PIPE, PipeAttachmentModel::encased);
        register(AllBlocks.GLASS_FLUID_PIPE, PipeAttachmentModel::new);
        register(AllBlocks.MECHANICAL_PUMP, PipeAttachmentModel::new);
        register(AllBlocks.FLUID_VALVE, PipeAttachmentModel::new);
        register(AllBlocks.SMART_FLUID_PIPE, PipeAttachmentModel::new);
        register(AllBlocks.FLUID_TANK, FluidTankModel::standard);
        register(AllBlocks.CREATIVE_FLUID_TANK, FluidTankModel::creative);
        register(AllBlocks.METAL_GIRDER, ConnectedGirderModel::new);
        register(AllBlocks.METAL_GIRDER_ENCASED_SHAFT, ConnectedGirderModel::new);
        register(AllBlocks.TABLE_CLOTH, TableClothModel::new);
        register(AllBlocks.ANDESITE_TABLE_CLOTH, TableClothModel::new);
        register(AllBlocks.BRASS_TABLE_CLOTH, TableClothModel::new);
        register(AllBlocks.COPPER_TABLE_CLOTH, TableClothModel::new);
        register(AllBlocks.FACTORY_GAUGE, FactoryPanelModel::new);
        register(AllBlocks.COPYCAT_STEP, CopycatStepModel::new);
        register(AllBlocks.COPYCAT_PANEL, CopycatPanelModel::new);
    }
}
