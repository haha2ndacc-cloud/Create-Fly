package com.zurrtum.create.content.kinetics.speedController;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementHelpers;
import com.zurrtum.create.catnip.placement.PlacementOffset;
import com.zurrtum.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.zurrtum.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.zurrtum.create.content.kinetics.simpleRelays.ICogWheel;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

public class SpeedControllerBlock extends HorizontalAxisKineticBlock implements IBE<SpeedControllerBlockEntity> {
    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());
    public static final BooleanProperty BRACKET = BooleanProperty.create("bracket");

    public SpeedControllerBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(BRACKET, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BRACKET);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState above = context.getLevel().getBlockState(context.getClickedPos().above());
        if (ICogWheel.isLargeCog(above) && above.getValue(CogWheelBlock.AXIS).isHorizontal()) {
            return defaultBlockState().setValue(
                HORIZONTAL_AXIS,
                above.getValue(CogWheelBlock.AXIS) == Axis.X ? Axis.Z : Axis.X
            ).setValue(BRACKET, true);
        }
        return super.getStateForPlacement(context);
    }

    @Override
    protected void neighborChanged(
        BlockState state,
        Level level,
        BlockPos pos,
        Block block,
        @Nullable Orientation orientation,
        boolean movedByPiston
    ) {
        BlockState stateAbove = level.getBlockState(pos.above());
        boolean hasBracket = stateAbove.getBlock() instanceof ICogWheel cogWheel && cogWheel.isDedicatedCogWheel() && cogWheel.isLargeCog() && stateAbove.getValue(
            CogWheelBlock.AXIS).isHorizontal();
        if (hasBracket != state.getValue(BRACKET)) {
            level.setBlockAndUpdate(pos, state.setValue(BRACKET, hasBracket));
        }
    }

    @Override
    protected InteractionResult useItemOn(
        ItemStack stack,
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        BlockHitResult hitResult
    ) {
        IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
        if (helper.matchesItem(stack)) {
            return helper.getOffset(player, level, state, pos, hitResult)
                .placeInWorld(level, (BlockItem) stack.getItem(), player, hand);
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return AllShapes.SPEED_CONTROLLER;
    }

    private static class PlacementHelper implements IPlacementHelper {
        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return ((Predicate<ItemStack>) ICogWheel::isLargeCogItem).and(ICogWheel::isDedicatedCogItem);
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return state -> state.is(AllBlocks.ROTATION_SPEED_CONTROLLER);
        }

        @Override
        public PlacementOffset getOffset(
            Player player,
            Level world,
            BlockState state,
            BlockPos pos,
            BlockHitResult ray
        ) {
            BlockPos newPos = pos.above();
            if (!world.getBlockState(newPos).canBeReplaced()) {
                return PlacementOffset.fail();
            }

            Axis newAxis = state.getValue(HORIZONTAL_AXIS) == Axis.X ? Axis.Z : Axis.X;

            if (!CogWheelBlock.isValidCogwheelPosition(true, world, newPos, newAxis)) {
                return PlacementOffset.fail();
            }

            return PlacementOffset.success(newPos, s -> s.setValue(CogWheelBlock.AXIS, newAxis));
        }
    }

    @Override
    public Class<SpeedControllerBlockEntity> getBlockEntityClass() {
        return SpeedControllerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SpeedControllerBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.ROTATION_SPEED_CONTROLLER;
    }
}
