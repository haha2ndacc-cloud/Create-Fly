package com.zurrtum.create.client.infrastructure.model;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TableClothModel extends WrapperBlockStateModel {
    private static final List<WeakReference<TableClothModel>> MODELS = new ArrayList<>(19);
    private static final Direction[] DIRECTIONS = new Direction[]{
        Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST
    };
    private static final int SOUTH_WEST = 0b0011;
    private static final int NORTH_WEST = 0b0110;
    private static final int NORTH_EAST = 0b1100;
    private static final int SOUTH_EAST = 0b1001;

    private final @Nullable BakedCorner[] corner = new BakedCorner[16];
    private @Nullable List<BakedQuad> south;
    private @Nullable List<BakedQuad> west;
    private @Nullable List<BakedQuad> north;
    private @Nullable List<BakedQuad> east;

    public TableClothModel(BlockState state, UnbakedRoot unbaked) {
        super(state, unbaked);
        MODELS.add(new WeakReference<>(this));
    }

    public static void reload() {
        MODELS.removeIf(ref -> {
            TableClothModel model = ref.get();
            if (model != null) {
                model.clearCache();
                return false;
            }
            return true;
        });
    }

    public void clearCache() {
        Arrays.fill(corner, null);
        south = west = north = east = null;
    }

    @Override
    public void addPartsWithInfo(
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState state,
        RandomSource random,
        List<BlockStateModelPart> parts
    ) {
        model.collectParts(random, parts);
        int index = 0;
        MutableBlockPos mutable = new MutableBlockPos();
        for (int i = 0; i < 4; i++) {
            Direction direction = DIRECTIONS[i];
            if (Block.shouldRenderFace(state, world.getBlockState(mutable.setWithOffset(pos, direction)), direction)) {
                index |= 1 << i;
            }
        }
        BakedCorner cache = corner[index];
        if (cache != null) {
            parts.add(cache);
            return;
        }
        Material.Baked material = model.particleMaterial();
        TextureAtlasSprite sprite = material.sprite();
        parts.add(corner[index] = new BakedCorner(
            (index & SOUTH_WEST) == SOUTH_WEST ? getSouth(random, sprite) : List.of(),
            (index & NORTH_WEST) == NORTH_WEST ? getWest(random, sprite) : List.of(),
            (index & NORTH_EAST) == NORTH_EAST ? getNorth(random, sprite) : List.of(),
            (index & SOUTH_EAST) == SOUTH_EAST ? getEast(random, sprite) : List.of(),
            material,
            model.materialFlags()
        ));
    }

    private List<BakedQuad> getSouth(RandomSource random, TextureAtlasSprite sprite) {
        if (south != null) {
            return south;
        }
        return south = replaceQuads(random, sprite, AllPartialModels.TABLE_CLOTH_SW);
    }

    private List<BakedQuad> getWest(RandomSource random, TextureAtlasSprite sprite) {
        if (west != null) {
            return west;
        }
        return west = replaceQuads(random, sprite, AllPartialModels.TABLE_CLOTH_NW);
    }

    private List<BakedQuad> getNorth(RandomSource random, TextureAtlasSprite sprite) {
        if (north != null) {
            return north;
        }
        return north = replaceQuads(random, sprite, AllPartialModels.TABLE_CLOTH_NE);
    }

    private List<BakedQuad> getEast(RandomSource random, TextureAtlasSprite sprite) {
        if (east != null) {
            return east;
        }
        return east = replaceQuads(random, sprite, AllPartialModels.TABLE_CLOTH_SE);
    }

    private static List<BakedQuad> replaceQuads(RandomSource random, TextureAtlasSprite replace, PartialModel model) {
        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
        List<BlockStateModelPart> parts = new ObjectArrayList<>();
        model.get().collectParts(random, parts);
        for (BlockStateModelPart part : parts) {
            for (Direction direction : DIRECTIONS) {
                for (BakedQuad quad : part.getQuads(direction)) {
                    builder.add(replaceQuad(replace, quad));
                }
            }
            for (BakedQuad quad : part.getQuads(null)) {
                builder.add(replaceQuad(replace, quad));
            }
        }
        return builder.build();
    }

    private static BakedQuad replaceQuad(TextureAtlasSprite replace, BakedQuad quad) {
        MaterialInfo info = quad.materialInfo();
        TextureAtlasSprite original = info.sprite();
        if (original == replace) {
            return quad;
        }
        return BakedModelHelper.replaceBakedQuadUV(
            quad,
            BakedModelHelper.calcSpriteUv(quad.packedUV0(), original, replace),
            BakedModelHelper.calcSpriteUv(quad.packedUV1(), original, replace),
            BakedModelHelper.calcSpriteUv(quad.packedUV2(), original, replace),
            BakedModelHelper.calcSpriteUv(quad.packedUV3(), original, replace),
            info
        );
    }

    private record BakedCorner(List<BakedQuad> south, List<BakedQuad> west, List<BakedQuad> north, List<BakedQuad> east,
                               Material.Baked particleMaterial, int materialFlags) implements BlockStateModelPart {
        @Override
        public List<BakedQuad> getQuads(@Nullable Direction side) {
            return switch (side) {
                case SOUTH -> south;
                case WEST -> west;
                case NORTH -> north;
                case EAST -> east;
                case null, default -> List.of();
            };
        }

        @Override
        public boolean useAmbientOcclusion() {
            return false;
        }
    }
}
