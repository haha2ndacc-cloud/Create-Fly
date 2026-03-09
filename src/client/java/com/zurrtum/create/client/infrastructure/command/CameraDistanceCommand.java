package com.zurrtum.create.client.infrastructure.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zurrtum.create.client.content.trains.CameraDistanceModifier;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class CameraDistanceCommand {
    public static LiteralArgumentBuilder<ClientSuggestionProvider> register() {
        return ClientCommand.literal("camera").then(ClientCommand.literal(
            "reset", context -> {
                CameraDistanceModifier.zoomOut(1);
                return Command.SINGLE_SUCCESS;
            }
        )).then(ClientCommand.argument(
            "multiplier", FloatArgumentType.floatArg(1), context -> {
                CameraDistanceModifier.zoomOut(FloatArgumentType.getFloat(context, "multiplier"));
                return Command.SINGLE_SUCCESS;
            }
        ));
    }
}
