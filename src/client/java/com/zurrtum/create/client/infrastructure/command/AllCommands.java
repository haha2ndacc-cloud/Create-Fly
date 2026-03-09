package com.zurrtum.create.client.infrastructure.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class AllCommands {
    public static void registerClient(CommandDispatcher<ClientSuggestionProvider> dispatcher) {
        LiteralArgumentBuilder<ClientSuggestionProvider> command = ClientCommand.literal("create");
        command.then(ToggleDebugCommand.register()).then(OverlayConfigCommand.register())
            .then(ClientCommand.literal("util").then(ClearBufferCacheCommand.register())
                .then(CameraDistanceCommand.register()).then(CameraAngleCommand.register()));
        dispatcher.register(command);
    }
}
