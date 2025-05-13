package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceInfo;
import de.blazemcworld.fireflow.space.SpaceManager;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PlayCommand {

    public static void register(CommandDispatcher<ServerCommandSource> cd) {
        register(cd, "play");
        register(cd, "join");
    }

    private static void register(CommandDispatcher<ServerCommandSource> cd, String alias) {
        cd.register(CommandManager.literal(alias)
                .executes(ctx -> {
                    ServerPlayerEntity player = CommandHelper.getPlayer(ctx.getSource());
                    Space space = CommandHelper.getSpace(player);
                    if (space == null) return Command.SINGLE_SUCCESS;

                    ModeManager.move(player, ModeManager.Mode.PLAY, space);
                    return Command.SINGLE_SUCCESS;
                })
                .then(CommandManager.argument("id", IntegerArgumentType.integer())
                        .executes(ctx -> {
                            ServerPlayerEntity player = CommandHelper.getPlayer(ctx.getSource());
                            if (player == null) return Command.SINGLE_SUCCESS;

                            int id = IntegerArgumentType.getInteger(ctx, "id");
                            SpaceInfo info = SpaceManager.getInfo(id);
                            if (info == null) {
                                player.sendMessage(Text.literal("Could not find space with id " + id + "!").formatted(Formatting.RED));
                                return Command.SINGLE_SUCCESS;
                            }

                            ModeManager.move(player, ModeManager.Mode.PLAY, SpaceManager.getOrLoadSpace(info));
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }

}
