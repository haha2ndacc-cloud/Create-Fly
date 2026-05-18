package com.zurrtum.create.client.infrastructure.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.ponder.Ponder;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;

public class ClearBufferCacheCommand {
    public static LiteralArgumentBuilder<ClientSuggestionProvider> register() {
        return ClientCommand.literal(
            "clearRenderBuffers", context -> {
                Ponder.invalidateRenderers();
                Create.invalidateRenderers();
                context.getSource().minecraft.gui.chatListener()
                    .handleSystemMessage(Component.literal("Cleared rendering buffers."), false);
                return Command.SINGLE_SUCCESS;
            }
        );
    }
}
