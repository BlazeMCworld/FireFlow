package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import de.blazemcworld.fireflow.messages.Messages;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.util.DummyPlayer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DummyCommand {

    public static void register(CommandDispatcher<ServerCommandSource> cd) {
        cd.register(CommandManager.literal("dummy")
                .then(CommandManager.literal("spawn")
                        .then(CommandManager.argument("id", IntegerArgumentType.integer(1, 5))
                                .executes(ctx -> {
                                    int id = IntegerArgumentType.getInteger(ctx, "id");
                                    ServerPlayerEntity player = CommandHelper.getPlayer(ctx.getSource());
                                    Space space = CommandHelper.getSpace(player);
                                    if (!CommandHelper.isDeveloperOrOwner(player, space)) return Command.SINGLE_SUCCESS;

                                    if (space.dummyManager.getDummy(id) != null) {
                                        Messages.sendMessage("That dummy has already been spawned!", Messages.ERROR, player);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    space.dummyManager.spawnDummy(id);
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("id", IntegerArgumentType.integer(1, 5))
                                .executes(ctx -> {
                                    int id = IntegerArgumentType.getInteger(ctx, "id");
                                    ServerPlayerEntity player = CommandHelper.getPlayer(ctx.getSource());
                                    Space space = CommandHelper.getSpace(player);
                                    if (!CommandHelper.isDeveloperOrOwner(player, space)) return Command.SINGLE_SUCCESS;

                                    DummyPlayer dummy = space.dummyManager.getDummy(id);
                                    if (dummy == null) {
                                        Messages.sendMessage("That dummy has not been spawned!", Messages.ERROR, player);
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
