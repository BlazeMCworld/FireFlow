package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.blazemcworld.fireflow.code.EditOrigin;
import de.blazemcworld.fireflow.space.Space;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class FunctionCommand {

    public static void register(CommandDispatcher<ServerCommandSource> cd) {
        cd.register(CommandManager.literal("function")
                .then(CommandManager.literal("create")
                        .then(CommandManager.argument("name", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ServerPlayerEntity player = CommandHelper.getPlayer(ctx.getSource());
                                    Space space = CommandHelper.getSpace(player);
                                    if (!CommandHelper.isInCode(player, space)) return Command.SINGLE_SUCCESS;
                                    space.editor.createFunction(EditOrigin.ofPlayer(player), StringArgumentType.getString(ctx, "name"));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(CommandManager.literal("delete")
                        .executes(ctx -> {
                            ServerPlayerEntity player = CommandHelper.getPlayer(ctx.getSource());
                            Space space = CommandHelper.getSpace(player);
                            if (!CommandHelper.isInCode(player, space)) return Command.SINGLE_SUCCESS;
                            space.editor.deleteFunction(EditOrigin.ofPlayer(player));
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(CommandManager.literal("icon")
                        .then(CommandManager.argument("icon", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ServerPlayerEntity player = CommandHelper.getPlayer(ctx.getSource());
                                    Space space = CommandHelper.getSpace(player);
                                    if (!CommandHelper.isInCode(player, space)) return Command.SINGLE_SUCCESS;
                                    space.editor.setFunctionIcon(EditOrigin.ofPlayer(player), StringArgumentType.getString(ctx, "icon"));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(CommandManager.literal("add")
                        .then(CommandManager.literal("input")
                                .then(CommandManager.argument("input", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            ServerPlayerEntity player = CommandHelper.getPlayer(ctx.getSource());
                                            Space space = CommandHelper.getSpace(player);
                                            if (!CommandHelper.isInCode(player, space)) return Command.SINGLE_SUCCESS;
                                            space.editor.addFunctionInput(EditOrigin.ofPlayer(player), StringArgumentType.getString(ctx, "input"));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        .then(CommandManager.literal("output")
                                .then(CommandManager.argument("output", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            ServerPlayerEntity player = CommandHelper.getPlayer(ctx.getSource());
                                            Space space = CommandHelper.getSpace(player);
                                            if (!CommandHelper.isInCode(player, space)) return Command.SINGLE_SUCCESS;
                                            space.editor.addFunctionOutput(EditOrigin.ofPlayer(player), StringArgumentType.getString(ctx, "output"));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
                .then(CommandManager.literal("remove")
                        .then(CommandManager.literal("input")
                                .then(CommandManager.argument("input", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            ServerPlayerEntity player = CommandHelper.getPlayer(ctx.getSource());
                                            Space space = CommandHelper.getSpace(player);
                                            if (!CommandHelper.isInCode(player, space)) return Command.SINGLE_SUCCESS;
                                            space.editor.removeFunctionInput(EditOrigin.ofPlayer(player), StringArgumentType.getString(ctx, "input"));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                                .then(CommandManager.literal("output")
                                        .then(CommandManager.argument("output", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    ServerPlayerEntity player = CommandHelper.getPlayer(ctx.getSource());
                                                    Space space = CommandHelper.getSpace(player);
                                                    if (!CommandHelper.isInCode(player, space))
                                                        return Command.SINGLE_SUCCESS;
                                                    space.editor.removeFunctionOutput(EditOrigin.ofPlayer(player), StringArgumentType.getString(ctx, "output"));
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
                )
        );
    }

}
