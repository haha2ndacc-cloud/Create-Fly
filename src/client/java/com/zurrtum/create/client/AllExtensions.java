package com.zurrtum.create.client;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.foundation.block.render.MultiPosDestructionHandler;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.waterwheel.WaterWheelStructuralBlock;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class AllExtensions {
    public static final Map<Block, MultiPosDestructionHandler> MULTI_POS = new IdentityHashMap<>();
    public static final Set<Block> BIG_OUTLINE = new HashSet<>();
    public static final Map<Item, ArmPose> ARM_POSE = new IdentityHashMap<>();

    public static void register() {
        MULTI_POS.put(
            AllBlocks.BELT, (level, pos, state, progress) -> {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof BeltBlockEntity belt) {
                    return new HashSet<>(BeltBlock.getBeltChain(level, belt.getController()));
                }
                return null;
            }
        );
        MULTI_POS.put(
            AllBlocks.WATER_WHEEL_STRUCTURAL, (level, pos, state, progress) -> {
                if (!AllBlocks.WATER_WHEEL_STRUCTURAL.stillValid(level, pos, state, false)) {
                    return null;
                }
                HashSet<BlockPos> set = new HashSet<>();
                set.add(WaterWheelStructuralBlock.getMaster(level, pos, state));
                return set;
            }
        );
        MULTI_POS.put(
            AllBlocks.TRACK, (level, pos, state, progress) -> {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof TrackBlockEntity track) {
                    return new HashSet<>(track.getConnections().keySet());
                }
                return null;
            }
        );
        BIG_OUTLINE.add(AllBlocks.CHAIN_CONVEYOR);
        BIG_OUTLINE.add(AllBlocks.ANDESITE_DOOR);
        BIG_OUTLINE.add(AllBlocks.BRASS_DOOR);
        BIG_OUTLINE.add(AllBlocks.COPPER_DOOR);
        BIG_OUTLINE.add(AllBlocks.TRAIN_DOOR);
        BIG_OUTLINE.add(AllBlocks.FRAMED_GLASS_DOOR);
        BIG_OUTLINE.add(AllBlocks.TRACK);
        AllBlocks.TABLE_CLOTH.forEach(BIG_OUTLINE::add);
        ARM_POSE.put(AllItems.POTATO_CANNON, ArmPose.CROSSBOW_HOLD);
        ARM_POSE.put(AllItems.WORLDSHAPER, ArmPose.CROSSBOW_HOLD);
    }
}
