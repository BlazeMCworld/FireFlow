package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.DataResult;
import de.blazemcworld.fireflow.inventory.ConfirmationMenu;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class SpaceCommand {

    public static void register(CommandDispatcher<CommandSourceStack> cd) {
        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal("space")
                .then(Commands.literal("icon")
                        .then(Commands.argument("icon", StringArgumentType.greedyString())
                                .suggests((ctx, builder) -> {
                                    for (Identifier id : BuiltInRegistries.ITEM.keySet()) {
                                        builder.suggest(id.getPath());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    ServerPlayer player = CommandHelper.getPlayer(ctx.getSource());
                                    Space space = CommandHelper.getSpace(player);
                                    if (!CommandHelper.isOwner(player, space)) return Command.SINGLE_SUCCESS;

                                    DataResult<Identifier> result = Identifier.read(StringArgumentType.getString(ctx, "icon"));
                                    if (result.isSuccess() && BuiltInRegistries.ITEM.containsKey(result.getOrThrow())) {
                                        space.info.icon = BuiltInRegistries.ITEM.getValue(result.getOrThrow());
                                        player.sendSystemMessage(Component.literal("Changed space icon!").withColor(TextColor.AQUA));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    player.sendSystemMessage(Component.literal("Invalid icon!").withColor(TextColor.RED));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("name")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ServerPlayer player = CommandHelper.getPlayer(ctx.getSource());
                                    Space space = CommandHelper.getSpace(player);
                                    if (!CommandHelper.isOwner(player, space)) return Command.SINGLE_SUCCESS;

                                    String name = StringArgumentType.getString(ctx, "name");
                                    if (name.length() > 256) {
                                        player.sendSystemMessage(Component.literal("Name too long!").withColor(TextColor.RED));
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    space.info.name = name;

                                    player.sendSystemMessage(Component.literal("Changed space name!").withColor(TextColor.AQUA));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("delete")
                        .executes(ctx -> {
                            ServerPlayer player = CommandHelper.getPlayer(ctx.getSource());
                            Space space = CommandHelper.getSpace(player);
                            if (!CommandHelper.isOwner(player, space)) return Command.SINGLE_SUCCESS;

                            ConfirmationMenu.open(player, "Delete this space?", () -> {
                                SpaceManager.delete(space);
                                player.sendSystemMessage(Component.literal("Deleted space!").withColor(TextColor.AQUA));
                            }, null);

                            return Command.SINGLE_SUCCESS;
                        })
                );
        MonitorCommand.attach(node);
        ContributorCommand.attach(node);
        VariablesCommand.attach(node);
        cd.register(node);
    }

}
