package com.zurrtum.create.client.infrastructure.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zurrtum.create.client.content.kinetics.KineticDebugger;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ToggleDebugCommand {
    public static LiteralArgumentBuilder<ClientSuggestionProvider> register() {
        return ClientCommand.literal(
            "rainbowDebug", context -> {
                Component text = Component.literal("Rainbow Debug Utility is currently: ")
                    .append(boolToText(KineticDebugger.rainbowDebug));
                context.getSource().minecraft.gui.chatListener().handleSystemMessage(text, false);
                return Command.SINGLE_SUCCESS;
            }
        ).then(ClientCommand.argument(
            "status", BoolArgumentType.bool(), context -> {
                KineticDebugger.rainbowDebug = BoolArgumentType.getBool(context, "status");
                Component text = boolToText(KineticDebugger.rainbowDebug).append(Component.literal(
                    " Rainbow Debug Utility").withStyle(ChatFormatting.WHITE));
                context.getSource().minecraft.gui.chatListener().handleSystemMessage(text, false);
                return Command.SINGLE_SUCCESS;
            }
        ));
    }

    private static MutableComponent boolToText(boolean b) {
        if (b) {
            return Component.literal("enabled").withStyle(ChatFormatting.GREEN);
        }
        return Component.literal("disabled").withStyle(ChatFormatting.RED);
    }
}
