package com.zurrtum.create.content.decoration.slidingDoor;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class SlidingDoorBlockEntity extends SmartBlockEntity {

    public LerpedFloat animation;
    boolean deferUpdate;

    public SlidingDoorBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.SLIDING_DOOR, pos, state);
        animation = LerpedFloat.linear().startWithValue(isOpen(state) ? 1 : 0);
    }

    @Override
    public void tick() {
        if (deferUpdate && !level.isClientSide()) {
            deferUpdate = false;
            BlockState blockState = getBlockState();
            blockState.handleNeighborChanged(level, worldPosition, Blocks.AIR, null, false);
        }

        super.tick();
        boolean open = isOpen(getBlockState());
        boolean wasSettled = animation.settled();
        animation.chase(open ? 1 : 0, 0.15f, Chaser.LINEAR);
        animation.tickChaser();

        if (level.isClientSide()) {
            return;
        }

        if (!open && !wasSettled && animation.settled()) {
            level.playSound(null, worldPosition, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 0.5f, 1);
        }
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(1);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    public static boolean isOpen(BlockState state) {
        return state.getValueOrElse(DoorBlock.OPEN, false);
    }

}
