package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.blazemcworld.fireflow.code.EditOrigin;
import de.blazemcworld.fireflow.space.Space;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class FunctionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> cd) {
        cd.register(Commands.literal("function")
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ServerPlayer player = CommandHelper.getPlayer(ctx.getSource());
                                    Space space = CommandHelper.getSpace(player);
                                    if (!CommandHelper.isInCode(player, space)) return Command.SINGLE_SUCCESS;
                                    space.editor.createFunction(EditOrigin.ofPlayer(player), StringArgumentType.getString(ctx, "name"));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("delete")
                        .executes(ctx -> {
                            ServerPlayer player = CommandHelper.getPlayer(ctx.getSource());
                            Space space = CommandHelper.getSpace(player);
                            if (!CommandHelper.isInCode(player, space)) return Command.SINGLE_SUCCESS;
                            space.editor.deleteFunction(EditOrigin.ofPlayer(player));
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("icon")
                        .then(Commands.argument("icon", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ServerPlayer player = CommandHelper.getPlayer(ctx.getSource());
                                    Space space = CommandHelper.getSpace(player);
                                    if (!CommandHelper.isInCode(player, space)) return Command.SINGLE_SUCCESS;
                                    space.editor.setFunctionIcon(EditOrigin.ofPlayer(player), StringArgumentType.getString(ctx, "icon"));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("add")
                        .then(Commands.literal("input")
                                .then(Commands.argument("input", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            ServerPlayer player = CommandHelper.getPlayer(ctx.getSource());
                                            Space space = CommandHelper.getSpace(player);
                                            if (!CommandHelper.isInCode(player, space)) return Command.SINGLE_SUCCESS;
                                            space.editor.addFunctionInput(EditOrigin.ofPlayer(player), StringArgumentType.getString(ctx, "input"));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        .then(Commands.literal("output")
                                .then(Commands.argument("output", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            ServerPlayer player = CommandHelper.getPlayer(ctx.getSource());
                                            Space space = CommandHelper.getSpace(player);
                                            if (!CommandHelper.isInCode(player, space)) return Command.SINGLE_SUCCESS;
                                            space.editor.addFunctionOutput(EditOrigin.ofPlayer(player), StringArgumentType.getString(ctx, "output"));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
                .then(Commands.literal("remove")
                        .then(Commands.literal("input")
                                .then(Commands.argument("input", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            ServerPlayer player = CommandHelper.getPlayer(ctx.getSource());
                                            Space space = CommandHelper.getSpace(player);
                                            if (!CommandHelper.isInCode(player, space)) return Command.SINGLE_SUCCESS;
                                            space.editor.removeFunctionInput(EditOrigin.ofPlayer(player), StringArgumentType.getString(ctx, "input"));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        .then(Commands.literal("output")
                                .then(Commands.argument("output", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            ServerPlayer player = CommandHelper.getPlayer(ctx.getSource());
                                            Space space = CommandHelper.getSpace(player);
                                            if (!CommandHelper.isInCode(player, space))
                                                return Command.SINGLE_SUCCESS;
                                            space.editor.removeFunctionOutput(EditOrigin.ofPlayer(player), StringArgumentType.getString(ctx, "output"));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
        );
    }

}
