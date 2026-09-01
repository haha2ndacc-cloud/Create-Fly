package com.zurrtum.create.infrastructure.fluids;

import com.zurrtum.create.AllFluids;
import com.zurrtum.create.AllItems;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

public class FluidBlock extends LiquidBlock {
    public FluidBlock(FlowingFluid fluid, Properties settings) {
        super(fluid, settings);
    }

    // Mirror water bucket pickup: empty bucket on honey/chocolate source gives honey/chocolate bucket like water gives water bucket
    @Override
    public ItemStack pickupBlock(LivingEntity entity, LevelAccessor level, BlockPos pos, BlockState state) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
        return getBucketForFluid();
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return super.getPickupSound();
    }

    private ItemStack getBucketForFluid() {
        // Explicit handling to mirror water bucket behaviour without relying on fluid.getBucket() field
        if (fluid == AllFluids.HONEY || fluid == AllFluids.FLOWING_HONEY) {
            return AllItems.HONEY_BUCKET.getDefaultInstance();
        }
        if (fluid == AllFluids.CHOCOLATE || fluid == AllFluids.FLOWING_CHOCOLATE) {
            return AllItems.CHOCOLATE_BUCKET.getDefaultInstance();
        }
        // Fallback to fluid's bucket (covers vanilla water/lava and other fluids that use field)
        return new ItemStack(fluid.getBucket());
    }


    @Override
    protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        world.scheduleTick(pos, state.getFluidState().getType(), fluid.getTickDelay(world));
    }

    @Override
    protected void neighborChanged(
        BlockState state,
        Level world,
        BlockPos pos,
        Block sourceBlock,
        @Nullable Orientation wireOrientation,
        boolean notify
    ) {
        world.scheduleTick(pos, state.getFluidState().getType(), fluid.getTickDelay(world));
    }
}
