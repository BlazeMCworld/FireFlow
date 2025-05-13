package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class LocateCommand {

    public static void register(CommandDispatcher<ServerCommandSource> cd) {
        register(cd, "locate");
        register(cd, "find");
    }

    private static void register(CommandDispatcher<ServerCommandSource> cd, String alias) {
        cd.register(CommandManager.literal(alias)
                .then(CommandManager.argument("player", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (ServerPlayerEntity player : FireFlow.server.getPlayerManager().getPlayerList()) {
                                builder.suggest(player.getGameProfile().getName());
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            ServerPlayerEntity target = FireFlow.server.getPlayerManager().getPlayer(StringArgumentType.getString(ctx, "player"));
                            if (target == null) {
                                ctx.getSource().sendMessage(Text.literal("Player not found!").formatted(Formatting.RED));
                                return Command.SINGLE_SUCCESS;
                            }

                            Space space = SpaceManager.getSpaceForPlayer(target);
                            ModeManager.Mode mode = ModeManager.getFor(target);

                            switch (mode) {
                                case LOBBY: {
                                    ctx.getSource().sendMessage(Text.literal(
                                            target.getGameProfile().getName() + " is currently in the lobby."
                                    ).formatted(Formatting.GREEN));
                                    break;
                                }
                                case PLAY: {
                                    ctx.getSource().sendMessage(Text.literal(
                                            target.getGameProfile().getName() + " is currently playing on space #" + space.info.id
                                    ).formatted(Formatting.GREEN));
                                    break;
                                }
                                case CODE: {
                                    ctx.getSource().sendMessage(Text.literal(
                                            target.getGameProfile().getName() + " is currently coding on space #" + space.info.id
                                    ).formatted(Formatting.GREEN));
                                    break;
                                }
                                case BUILD: {
                                    ctx.getSource().sendMessage(Text.literal(
                                            target.getGameProfile().getName() + " is currently building on space #" + space.info.id
                                    ).formatted(Formatting.GREEN));
                                    break;
                                }
                            }

                            return Command.SINGLE_SUCCESS;
                        }))
        );
    }

}
