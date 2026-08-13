package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.util.DummyPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;

public class DummyCommand {

    public static void register(CommandDispatcher<CommandSourceStack> cd) {
        cd.register(Commands.literal("dummy")
                .then(Commands.literal("spawn")
                        .then(Commands.argument("id", IntegerArgumentType.integer(1, 5))
                                .executes(ctx -> {
                                    int id = IntegerArgumentType.getInteger(ctx, "id");
                                    ServerPlayer player = CommandHelper.getPlayer(ctx.getSource());
                                    Space space = CommandHelper.getSpace(player);
                                    if (!CommandHelper.isDeveloperOrOwner(player, space)) return Command.SINGLE_SUCCESS;

                                    if (space.dummyManager.getDummy(id) != null) {
                                        player.sendSystemMessage(Component.literal("That dummy has already been spawned!").withColor(TextColor.RED));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    space.dummyManager.spawnDummy(id);
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("id", IntegerArgumentType.integer(1, 5))
                                .executes(ctx -> {
                                    int id = IntegerArgumentType.getInteger(ctx, "id");
                                    ServerPlayer player = CommandHelper.getPlayer(ctx.getSource());
                                    Space space = CommandHelper.getSpace(player);
                                    if (!CommandHelper.isDeveloperOrOwner(player, space)) return Command.SINGLE_SUCCESS;

                                    DummyPlayer dummy = space.dummyManager.getDummy(id);
                                    if (dummy == null) {
                                        player.sendSystemMessage(Component.literal("That dummy has not been spawned!").withColor(TextColor.RED));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    dummy.discard();
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
        );
    }

}
