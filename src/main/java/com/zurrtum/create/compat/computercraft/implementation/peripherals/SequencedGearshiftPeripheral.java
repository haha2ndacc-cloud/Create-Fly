package com.zurrtum.create.compat.computercraft.implementation.peripherals;

import com.zurrtum.create.content.kinetics.transmission.sequencer.Instruction;
import com.zurrtum.create.content.kinetics.transmission.sequencer.InstructionSpeedModifiers;
import com.zurrtum.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlockEntity;
import com.zurrtum.create.content.kinetics.transmission.sequencer.SequencerInstructions;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;

public class SequencedGearshiftPeripheral extends SyncedPeripheral<SequencedGearshiftBlockEntity> {

    public SequencedGearshiftPeripheral(SequencedGearshiftBlockEntity blockEntity) {
        super(blockEntity);
    }

    @LuaFunction(mainThread = true)
    public final void rotate(IArguments arguments) throws LuaException {
        runInstruction(arguments, SequencerInstructions.TURN_ANGLE);
    }

    @LuaFunction(mainThread = true)
    public final void move(IArguments arguments) throws LuaException {
        runInstruction(arguments, SequencerInstructions.TURN_DISTANCE);
    }

    @LuaFunction
    public final boolean isRunning() {
        return !blockEntity.isIdle();
    }

    private void runInstruction(IArguments arguments, SequencerInstructions instructionType) throws LuaException {
        int speedModifier = arguments.count() > 1 ? arguments.getInt(1) : 1;
        blockEntity.getInstructions().clear();

        blockEntity.getInstructions().add(new Instruction(
            instructionType,
            InstructionSpeedModifiers.getByModifier(speedModifier),
            Math.abs(arguments.getInt(0))
        ));
        blockEntity.getInstructions().add(new Instruction(SequencerInstructions.END));

        blockEntity.run(0);
    }

    @Override
    public String getType() {
        return "Create_SequencedGearshift";
    }

}
