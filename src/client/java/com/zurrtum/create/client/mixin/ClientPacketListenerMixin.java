package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.zurrtum.create.client.flywheel.impl.FlwCommands;
import com.zurrtum.create.client.infrastructure.command.AllCommands;
import com.zurrtum.create.client.infrastructure.command.ClientCommand;
import com.zurrtum.create.client.ponder.command.PonderCommands;
import com.zurrtum.create.foundation.blockEntity.SyncedBlockEntity;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.SignableCommand;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Shadow
    private CommandDispatcher<ClientSuggestionProvider> commands;

    @Inject(method = "handleCommands(Lnet/minecraft/network/protocol/game/ClientboundCommandsPacket;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;commands:Lcom/mojang/brigadier/CommandDispatcher;", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void addCommand(ClientboundCommandsPacket packet, CallbackInfo ci) {
        FlwCommands.registerClientCommands(commands);
        PonderCommands.registerClient(commands);
        AllCommands.registerClient(commands);
    }

    @WrapOperation(method = "sendCommand(Ljava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/SignableCommand;of(Lcom/mojang/brigadier/ParseResults;)Lnet/minecraft/network/chat/SignableCommand;"))
    private SignableCommand<ClientSuggestionProvider> parseCommand(
        ParseResults<ClientSuggestionProvider> command,
        Operation<SignableCommand<ClientSuggestionProvider>> original,
        @Local(argsOnly = true) String commandString
    ) {
        if (command.getContext().getCommand() instanceof ClientCommand clientCommand) {
            clientCommand.run(command, commandString);
            return null;
        }
        return original.call(command);
    }

    @Inject(method = "sendCommand(Ljava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/SignableCommand;arguments()Ljava/util/List;"), cancellable = true)
    private void checkCommand(CallbackInfo ci, @Local SignableCommand<ClientSuggestionProvider> signableCommand) {
        if (signableCommand == null) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "lambda$handleBlockEntityData$0(Lnet/minecraft/network/protocol/game/ClientboundBlockEntityDataPacket;Lnet/minecraft/world/level/block/entity/BlockEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;loadWithComponents(Lnet/minecraft/world/level/storage/ValueInput;)V"))
    private void onDataPacket(BlockEntity blockEntity, ValueInput input, Operation<Void> original) {
        if (blockEntity instanceof SyncedBlockEntity syncedBlockEntity) {
            syncedBlockEntity.onDataPacket(input);
        } else {
            original.call(blockEntity, input);
        }
    }
}
