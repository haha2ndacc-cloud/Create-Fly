package com.zurrtum.create.content.kinetics.crank;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.foundation.utility.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class ValveHandleBlock extends HandCrankBlock {

    public final @Nullable DyeColor color;

    public static ValveHandleBlock copper(Properties properties) {
        return new ValveHandleBlock(null, properties);
    }

    public ValveHandleBlock(@Nullable DyeColor color, Properties properties) {
        super(properties);
        this.color = color;
    }

    @Override
    protected boolean shouldChangedStateKeepBlockEntity(BlockState blockState) {
        return AllBlockEntityTypes.VALVE_HANDLE.isValid(blockState);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return AllShapes.VALVE_HANDLE.get(pState.getValue(FACING));
    }

    public static ValveHandleBlock getColorBlock(@Nullable DyeColor color) {
        if (color == null) {
            return AllBlocks.COPPER_VALVE_HANDLE;
        }
        return AllBlocks.VALVE_HANDLE.pick(color);
    }

    public void clicked(Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        useItemOn(heldItem, state, level, pos, player, hand, null);
    }

    @Override
    protected InteractionResult useItemOn(
        ItemStack heldItem,
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        @Nullable BlockHitResult hitResult
    ) {
        DyeColor color = AllItemTags.getDyeColor(heldItem);

        if (color != null && color != this.color) {
            if (!level.isClientSide()) {
                level.setBlockAndUpdate(
                    pos,
                    BlockHelper.copyProperties(state, getColorBlock(color).defaultBlockState())
                );
            }
            return InteractionResult.SUCCESS;
        }

        onBlockEntityUse(
            level,
            pos,
            hcbe -> hcbe instanceof ValveHandleBlockEntity vhbe && vhbe.activate(player.isShiftKeyDown()) ?
                InteractionResult.SUCCESS : InteractionResult.PASS
        );
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(
        BlockState state,
        Level world,
        BlockPos pos,
        Player player,
        BlockHitResult hit
    ) {
        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    public BlockEntityType<? extends HandCrankBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.VALVE_HANDLE;
    }
}
