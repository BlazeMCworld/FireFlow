package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.DataResult;
import de.blazemcworld.fireflow.inventory.ConfirmationMenu;
import de.blazemcworld.fireflow.messages.Messages;
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
                                        Messages.sendMessage(
                                                "Changed space icon to <white><lang:"
                                                        + space.info.icon.getTranslationKey()
                                                        + "><default>!",
                                                Messages.SUCCESS, player
                                        );
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    Messages.sendMessage("Invalid icon!", Messages.INFO, player);
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
                                        Messages.sendMessage("Name too long!", Messages.ERROR, player);
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    space.info.name = name;

                                    Messages.sendMessage(
                                            "Changed space name to " + Messages.escapeMiniMessage(name, Messages.SUCCESS) + "!",
                                            Messages.SUCCESS, player
                                    );
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
                                Messages.sendMessage("Deleted space!", Messages.SUCCESS, player);
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
