package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.client.compat.fabric.WrapperModel;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class WrapperBlockStateModel implements BlockStateModel, BlockStateModel.UnbakedRoot {
    private static final boolean FABRIC = FabricLoader.getInstance().isModLoaded("fabric-model-loading-api-v1");
    @UnknownNullability
    protected BlockStateModel model;
    protected @Nullable Entry entry;

    public WrapperBlockStateModel() {
    }

    public WrapperBlockStateModel(BlockState state, UnbakedRoot unbaked) {
        entry = new Entry(state, unbaked);
    }

    public BlockStateModel extractRenderModel(BlockAndTintGetter world, BlockPos pos, BlockState state, long seed) {
        return BlockStateRenderModel.create(this, world, pos, state, seed);
    }

    public void addPartsWithInfo(
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState state,
        RandomSource random,
        List<BlockStateModelPart> parts
    ) {
        collectParts(random, parts);
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
        model.collectParts(random, parts);
    }

    public boolean needUpdateTerrainParticle() {
        return false;
    }

    public Material.Baked particleMaterialWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state) {
        return particleMaterial();
    }

    @Override
    public Material.Baked particleMaterial() {
        return model.particleMaterial();
    }

    @Override
    public int materialFlags() {
        return model.materialFlags();
    }

    @Override
    public BlockStateModel bake(BlockState state, ModelBaker baker) {
        if (entry != null) {
            model = entry.bake(baker);
            entry = null;
        }
        return this;
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        if (entry != null) {
            entry.resolveDependencies(resolver);
        }
    }

    @Override
    public Object visualEqualityGroup(BlockState state) {
        return this;
    }

    protected record Entry(BlockState state, UnbakedRoot unbaked) {
        public BlockStateModel bake(ModelBaker baker) {
            return unbaked.bake(state, baker);
        }

        public void resolveDependencies(Resolver resolver) {
            unbaked.resolveDependencies(resolver);
        }
    }

    public static BlockStateModel unwrapCompat(BlockStateModel model) {
        if (FABRIC) {
            while (model instanceof WrapperModel wrapper) {
                BlockStateModel child = wrapper.create$getWrapped();
                if (child == model) {
                    break;
                }
                model = child;
            }
        }
        return model;
    }

    private static class BlockStateRenderModel implements BlockStateModel {
        private static final BlockStateModel INSTANCE = new BlockStateRenderModel();
        private static final RandomSource RANDOM = RandomSource.createThreadLocalInstance(0L);

        public static BlockStateModel create(
            WrapperBlockStateModel model,
            BlockAndTintGetter world,
            BlockPos pos,
            BlockState state,
            long seed
        ) {
            RANDOM.setSeed(seed);
            ArrayList<BlockStateModelPart> parts = new ArrayList<>();
            model.addPartsWithInfo(world, pos, state, RANDOM, parts);
            return switch (parts.size()) {
                case 0 -> INSTANCE;
                case 1 -> new Single(parts.getFirst());
                default -> new Multiple(parts);
            };
        }

        @Override
        public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
        }

        @Override
        @SuppressWarnings("DataFlowIssue")
        public Material.Baked particleMaterial() {
            return null;
        }

        @Override
        public int materialFlags() {
            return 0;
        }

        private static class Single extends BlockStateRenderModel {
            private final BlockStateModelPart part;

            private Single(BlockStateModelPart part) {
                this.part = part;
            }

            @Override
            public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
                output.add(part);
            }
        }

        private static class Multiple extends BlockStateRenderModel {
            private final List<BlockStateModelPart> parts;

            private Multiple(List<BlockStateModelPart> parts) {
                this.parts = parts;
            }

            @Override
            public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
                output.addAll(parts);
            }
        }
    }
}
