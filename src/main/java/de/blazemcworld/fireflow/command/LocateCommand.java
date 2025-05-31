package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.messages.Messages;
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
                .executes(ctx -> {
                    ServerPlayerEntity target = CommandHelper.getPlayer(ctx.getSource());
                    return target == null ? Command.SINGLE_SUCCESS : locateAndRespond(target, ctx);
                })
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
                                Messages.sendMessage("Player not found!", Messages.ERROR, ctx.getSource());
                                return Command.SINGLE_SUCCESS;
                            }

                            return locateAndRespond(target, ctx);
                        }))
        );
    }

    /**
     * Locates the target player and sends a response to the command source
     * @param target The player to locate
     * @param ctx The command context
     * @return The success code, hardcoded to <code>Command.SINGLE_SUCCESS</code>
     */
    private static int locateAndRespond(ServerPlayerEntity target, CommandContext<ServerCommandSource> ctx) {
        Space space = SpaceManager.getSpaceForPlayer(target);
        ModeManager.Mode mode = ModeManager.getFor(target);

        switch (mode) {
            case LOBBY: {
                Messages.sendMessage(
                        target.getGameProfile().getName() + " is currently in the lobby.",
                        Messages.INFO, ctx.getSource()
                );
                break;
            }
            case PLAY: {
                Messages.sendMessage(
                        target.getGameProfile().getName() + " is currently <white>playing<default> on space <white>#" + space.info.id,
                        Messages.INFO, ctx.getSource()
                );
                break;
            }
            case CODE: {
                Messages.sendMessage(
                        target.getGameProfile().getName() + " is currently <white>coding<default> on space <white>#" + space.info.id,
                        Messages.INFO, ctx.getSource()
                );
                break;
            }
            case BUILD: {
                Messages.sendMessage(
                        target.getGameProfile().getName() + " is currently <white>building<default> on space <white>#" + space.info.id,
                        Messages.INFO, ctx.getSource()
                );
                break;
            }
        }

        return Command.SINGLE_SUCCESS;
    }

}