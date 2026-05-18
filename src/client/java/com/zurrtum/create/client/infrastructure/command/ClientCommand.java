package com.zurrtum.create.client.infrastructure.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.*;

public interface ClientCommand extends Command<ClientSuggestionProvider> {
    default void run(ParseResults<ClientSuggestionProvider> command, String commandString) {
        try {
            if (command.getExceptions().size() == 1) {
                throw command.getExceptions().values().iterator().next();
            }
            run(command.getContext().build(commandString));
        } catch (CommandSyntaxException e) {
            CommandExceptionType type = e.getType();
            if (type != CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand() && type != CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException()) {
                command.getContext().getSource().minecraft.gui.chatListener()
                    .handleSystemMessage(
                        Component.empty().append(ComponentUtils.fromMessage(e.getRawMessage()))
                            .withStyle(ChatFormatting.RED), false
                    );
                if (e.getInput() != null && e.getCursor() >= 0) {
                    int cursor = Math.min(e.getInput().length(), e.getCursor());
                    MutableComponent context = Component.empty().withStyle(ChatFormatting.GRAY)
                        .withStyle(s -> s.withClickEvent(new ClickEvent.SuggestCommand("/" + commandString)));
                    if (cursor > 10) {
                        context.append(CommonComponents.ELLIPSIS);
                    }
                    context.append(e.getInput().substring(Math.max(0, cursor - 10), cursor));
                    if (cursor < e.getInput().length()) {
                        Component remaining = Component.literal(e.getInput().substring(cursor))
                            .withStyle(ChatFormatting.RED, ChatFormatting.UNDERLINE);
                        context.append(remaining);
                    }
                    context.append(Component.translatable("command.context.here")
                        .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
                    command.getContext().getSource().minecraft.gui.chatListener()
                        .handleSystemMessage(Component.empty().append(context).withStyle(ChatFormatting.RED), false);
                }
            }
        } catch (Exception e) {
            String message = e.getMessage();
            Component text =
                message == null ? CommonComponents.EMPTY : Component.literal(message).withStyle(ChatFormatting.RED);
            command.getContext().getSource().minecraft.gui.chatListener().handleSystemMessage(text, false);
        }
    }

    static LiteralArgumentBuilder<ClientSuggestionProvider> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    static LiteralArgumentBuilder<ClientSuggestionProvider> literal(String name, ClientCommand command) {
        return LiteralArgumentBuilder.<ClientSuggestionProvider>literal(name).executes(command);
    }

    static <T> RequiredArgumentBuilder<ClientSuggestionProvider, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    static <T> RequiredArgumentBuilder<ClientSuggestionProvider, T> argument(
        String name,
        ArgumentType<T> type,
        ClientCommand command
    ) {
        return RequiredArgumentBuilder.<ClientSuggestionProvider, T>argument(name, type).executes(command);
    }
}
