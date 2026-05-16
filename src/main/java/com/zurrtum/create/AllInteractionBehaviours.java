package com.zurrtum.create;

import com.zurrtum.create.api.behaviour.interaction.ConductorBlockInteractionBehavior;
import com.zurrtum.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsMovingInteraction;
import com.zurrtum.create.content.contraptions.actors.seat.SeatInteractionBehaviour;
import com.zurrtum.create.content.contraptions.actors.trainControls.ControlsInteractionBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.DoorMovingInteraction;
import com.zurrtum.create.content.contraptions.behaviour.LeverMovingInteraction;
import com.zurrtum.create.content.contraptions.behaviour.TrapdoorMovingInteraction;
import com.zurrtum.create.content.kinetics.deployer.DeployerMovingInteraction;
import com.zurrtum.create.content.logistics.depot.MountedDepotInteractionBehaviour;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ColorCollection;

public class AllInteractionBehaviours {
    public static final ConductorBlockInteractionBehavior.BlazeBurner BLAZE_BURNER = new ConductorBlockInteractionBehavior.BlazeBurner();
    public static final MountedDepotInteractionBehaviour DEPOT = new MountedDepotInteractionBehaviour();
    public static final ContraptionControlsMovingInteraction CONTRAPTION_CONTROLS = new ContraptionControlsMovingInteraction();
    public static final DeployerMovingInteraction DEPLOYER = new DeployerMovingInteraction();
    public static final ControlsInteractionBehaviour CONTROLS = new ControlsInteractionBehaviour();
    public static final DoorMovingInteraction DOOR = new DoorMovingInteraction();
    public static final SeatInteractionBehaviour SEAT = new SeatInteractionBehaviour();
    public static final LeverMovingInteraction LEVER = new LeverMovingInteraction();
    public static final TrapdoorMovingInteraction TRAPDOOR = new TrapdoorMovingInteraction();

    public static void register(MovingInteractionBehaviour behaviour, Block... blocks) {
        for (Block block : blocks) {
            MovingInteractionBehaviour.REGISTRY.register(block, behaviour);
        }
    }

    public static void register(MovingInteractionBehaviour behaviour, ColorCollection<? extends Block> collection) {
        collection.forEach(block -> MovingInteractionBehaviour.REGISTRY.register(block, behaviour));
    }

    @SuppressWarnings("deprecation")
    public static void register(MovingInteractionBehaviour behaviour, TagKey<Block> tag) {
        MovingInteractionBehaviour.REGISTRY.registerProvider(block -> block.builtInRegistryHolder().is(tag) ?
            behaviour : null);
    }

    public static void register() {
        register(BLAZE_BURNER, AllBlocks.BLAZE_BURNER);
        register(DEPOT, AllBlocks.DEPOT);
        register(CONTRAPTION_CONTROLS, AllBlocks.CONTRAPTION_CONTROLS);
        register(DEPLOYER, AllBlocks.DEPLOYER);
        register(CONTROLS, AllBlocks.TRAIN_CONTROLS);
        register(
            DOOR,
            AllBlocks.ANDESITE_DOOR,
            AllBlocks.BRASS_DOOR,
            AllBlocks.COPPER_DOOR,
            AllBlocks.TRAIN_DOOR,
            AllBlocks.FRAMED_GLASS_DOOR
        );
        register(DOOR, BlockTags.WOODEN_DOORS);
        register(SEAT, AllBlocks.SEAT);
        register(LEVER, Blocks.LEVER);
        register(TRAPDOOR, AllBlocks.TRAIN_TRAPDOOR, AllBlocks.FRAMED_GLASS_TRAPDOOR);
        register(TRAPDOOR, BlockTags.WOODEN_TRAPDOORS);
        register(TRAPDOOR, BlockTags.FENCE_GATES);
    }
}
