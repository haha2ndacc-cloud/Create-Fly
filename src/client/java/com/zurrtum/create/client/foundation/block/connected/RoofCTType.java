package com.zurrtum.create.client.foundation.block.connected;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StairsShape;

import static com.zurrtum.create.Create.MOD_ID;

public class RoofCTType extends CTType {
    private static final int UP = UP_FLAG;
    private static final int DOWN = DOWN_FLAG;
    private static final int LEFT = LEFT_FLAG;
    private static final int RIGHT = RIGHT_FLAG;
    private static final int TOP_LEFT = TOP_LEFT_FLAG;
    private static final int TOP_RIGHT = TOP_RIGHT_FLAG;
    private static final int BOTTOM_LEFT = BOTTOM_LEFT_FLAG;
    private static final int BOTTOM_RIGHT = BOTTOM_RIGHT_FLAG;
    private static final int UP_DOWN_LEFT_RIGHT = UP | DOWN | LEFT | RIGHT;
    private static final int TOP_LEFT_RIGHT = TOP_LEFT | TOP_RIGHT;
    private static final int BOTTOM_LEFT_RIGHT = BOTTOM_LEFT | BOTTOM_RIGHT;
    private static final int INNER_LEFT = StairsShape.INNER_LEFT.ordinal() << 2;
    private static final int INNER_RIGHT = StairsShape.INNER_RIGHT.ordinal() << 2;
    private static final int OUTER_LEFT = StairsShape.OUTER_LEFT.ordinal() << 2;
    private static final int OUTER_RIGHT = StairsShape.OUTER_RIGHT.ordinal() << 2;
    private static final int SOUTH = Direction.SOUTH.get2DDataValue();
    private static final int WEST = Direction.WEST.get2DDataValue();
    private static final int NORTH = Direction.NORTH.get2DDataValue();
    private static final int EAST = Direction.EAST.get2DDataValue();

    public static final int[] MAP;
    public static final int[] STAIR_MAP;
    public static final int SIZE;

    private static void mapping(int index, int flags, int... stairIndices) {
        MAP[flags] = index;
        for (int stairIndex : stairIndices) {
            STAIR_MAP[stairIndex] = flags;
        }
    }

    static {
        MAP = new int[ALL_FLAGS + 1];
        STAIR_MAP = new int[20];
        int index = 0;
        mapping(++index, DOWN | RIGHT | BOTTOM_RIGHT, OUTER_LEFT | NORTH, OUTER_RIGHT | EAST);
        mapping(++index, DOWN | LEFT | RIGHT, SOUTH);
        mapping(++index, DOWN | LEFT | BOTTOM_LEFT, OUTER_LEFT | EAST, OUTER_RIGHT | SOUTH);
        mapping(++index, UP | DOWN | RIGHT, EAST);
        mapping(++index, UP | DOWN | LEFT, WEST);
        mapping(++index, UP | RIGHT | TOP_RIGHT, OUTER_LEFT | WEST, OUTER_RIGHT | NORTH);
        mapping(++index, UP | LEFT | RIGHT, NORTH);
        mapping(++index, UP | LEFT | TOP_LEFT, OUTER_LEFT | SOUTH, OUTER_RIGHT | WEST);
        mapping(++index, UP_DOWN_LEFT_RIGHT | TOP_LEFT_RIGHT | BOTTOM_LEFT, INNER_LEFT | SOUTH, INNER_RIGHT | WEST);
        mapping(++index, UP_DOWN_LEFT_RIGHT | TOP_LEFT_RIGHT | BOTTOM_RIGHT, INNER_LEFT | WEST, INNER_RIGHT | NORTH);
        mapping(++index, UP_DOWN_LEFT_RIGHT | TOP_LEFT | BOTTOM_LEFT_RIGHT, INNER_LEFT | EAST, INNER_RIGHT | SOUTH);
        mapping(++index, UP_DOWN_LEFT_RIGHT | TOP_RIGHT | BOTTOM_LEFT_RIGHT, INNER_LEFT | NORTH, INNER_RIGHT | EAST);
        SIZE = index + 1;
    }

    public RoofCTType() {
        super(Identifier.fromNamespaceAndPath(MOD_ID, "roof"), SIZE, ALL);
    }

    public int getStairMapping(BlockState state) {
        return STAIR_MAP[state.getValue(StairBlock.SHAPE).ordinal() << 2 | state.getValue(StairBlock.FACING)
            .get2DDataValue()];
    }

    @Override
    public int getTextureIndex(int context) {
        return MAP[context & ALL_FLAGS];
    }
}
