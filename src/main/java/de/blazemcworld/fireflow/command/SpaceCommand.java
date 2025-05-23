package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.DataResult;
import de.blazemcworld.fireflow.inventory.ConfirmationMenu;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class SpaceCommand {

    public static void register(CommandDispatcher<ServerCommandSource> cd) {
        LiteralArgumentBuilder<ServerCommandSource> node = CommandManager.literal("space")
                .then(CommandManager.literal("icon")
                        .then(CommandManager.argument("icon", StringArgumentType.greedyString())
                                .suggests((ctx, builder) -> {
                                    for (Identifier id : Registries.ITEM.getIds()) {
                                        builder.suggest(id.getPath());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    ServerPlayerEntity player = CommandHelper.getPlayer(ctx.getSource());
                                    Space space = CommandHelper.getSpace(player);
                                    if (!CommandHelper.isOwner(player, space)) return Command.SINGLE_SUCCESS;

                                    DataResult<Identifier> result = Identifier.validate(StringArgumentType.getString(ctx, "icon"));
                                    if (result.isSuccess() && Registries.ITEM.containsId(result.getOrThrow())) {
                                        space.info.icon = Registries.ITEM.get(result.getOrThrow());
                                        player.sendMessage(Text.literal("Changed space icon!").formatted(Formatting.AQUA));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    player.sendMessage(Text.literal("Invalid icon!").formatted(Formatting.RED));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(CommandManager.literal("name")
                        .then(CommandManager.argument("name", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ServerPlayerEntity player = CommandHelper.getPlayer(ctx.getSource());
                                    Space space = CommandHelper.getSpace(player);
                                    if (!CommandHelper.isOwner(player, space)) return Command.SINGLE_SUCCESS;

                                    String name = StringArgumentType.getString(ctx, "name");
                                    if (name.length() > 256) {
                                        player.sendMessage(Text.literal("Name too long!").formatted(Formatting.RED));
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    space.info.name = name;

                                    player.sendMessage(Text.literal("Changed space name!").formatted(Formatting.AQUA));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(CommandManager.literal("delete")
                        .executes(ctx -> {
                            ServerPlayerEntity player = CommandHelper.getPlayer(ctx.getSource());
                            Space space = CommandHelper.getSpace(player);
                            if (!CommandHelper.isOwner(player, space)) return Command.SINGLE_SUCCESS;

                            ConfirmationMenu.open(player, "Delete this space?", () -> {
                                SpaceManager.delete(space);
                                player.sendMessage(Text.literal("Deleted space!").formatted(Formatting.AQUA));
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
