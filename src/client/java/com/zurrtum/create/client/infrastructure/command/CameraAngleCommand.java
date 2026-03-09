package com.zurrtum.create.client.infrastructure.command;


import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zurrtum.create.client.foundation.utility.CameraAngleAnimationService;
import com.zurrtum.create.client.foundation.utility.CameraAngleAnimationService.Mode;
import com.zurrtum.create.client.foundation.utility.CameraAngleAnimationService.ModeArgument;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class CameraAngleCommand {
    public static LiteralArgumentBuilder<ClientSuggestionProvider> register() {
        return ClientCommand.literal("angle").then(ClientCommand.literal("yaw").then(ClientCommand.argument(
            "degrees", FloatArgumentType.floatArg(), context -> {
                CameraAngleAnimationService.setYawTarget(FloatArgumentType.getFloat(context, "degrees"));
                return Command.SINGLE_SUCCESS;
            }
        ))).then(ClientCommand.literal("pitch").then(ClientCommand.argument(
            "degrees", FloatArgumentType.floatArg(), context -> {
                CameraAngleAnimationService.setPitchTarget(FloatArgumentType.getFloat(context, "degrees"));
                return Command.SINGLE_SUCCESS;
            }
        ))).then(ClientCommand.literal("mode").then(ClientCommand.argument(
            "mode", ModeArgument.INSTANCE, context -> {
                CameraAngleAnimationService.setAnimationMode(context.getArgument("mode", Mode.class));
                return Command.SINGLE_SUCCESS;
            }
        ).then(ClientCommand.argument(
            "speed", FloatArgumentType.floatArg(0), context -> {
                CameraAngleAnimationService.setAnimationMode(context.getArgument("mode", Mode.class));
                CameraAngleAnimationService.setAnimationSpeed(FloatArgumentType.getFloat(context, "speed"));
                return Command.SINGLE_SUCCESS;
            }
        ))));
    }
}
