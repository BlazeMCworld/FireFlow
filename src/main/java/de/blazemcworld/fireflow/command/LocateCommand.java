package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;

public class LocateCommand {

    public static void register(CommandDispatcher<CommandSourceStack> cd) {
        register(cd, "locate");
        register(cd, "find");
    }

    private static void register(CommandDispatcher<CommandSourceStack> cd, String alias) {
        cd.register(Commands.literal(alias)
                .executes(ctx -> {
                    ServerPlayer target = CommandHelper.getPlayer(ctx.getSource());
                    return target == null ? Command.SINGLE_SUCCESS : locateAndRespond(target, ctx);
                })
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (ServerPlayer player : FireFlow.server.getPlayerList().getPlayers()) {
                                builder.suggest(player.getGameProfile().name());
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            ServerPlayer target = FireFlow.server.getPlayerList().getPlayer(StringArgumentType.getString(ctx, "player"));
                            if (target == null) {
                                ctx.getSource().sendSystemMessage(Component.literal("Player not found!").withColor(TextColor.RED));
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
    private static int locateAndRespond(ServerPlayer target, CommandContext<CommandSourceStack> ctx) {
        Space space = SpaceManager.getSpaceForPlayer(target);
        ModeManager.Mode mode = ModeManager.getFor(target);

        switch (mode) {
            case LOBBY: {
                ctx.getSource().sendSystemMessage(Component.literal(
                        target.getGameProfile().name() + " is currently in the lobby."
                ).withColor(TextColor.GREEN));
                break;
            }
            case PLAY: {
                ctx.getSource().sendSystemMessage(Component.literal(
                        target.getGameProfile().name() + " is currently playing on space #" + space.info.id
                ).withColor(TextColor.GREEN));
                break;
            }
            case CODE: {
                ctx.getSource().sendSystemMessage(Component.literal(
                        target.getGameProfile().name() + " is currently coding on space #" + space.info.id
                ).withColor(TextColor.GREEN));
                break;
            }
            case BUILD: {
                ctx.getSource().sendSystemMessage(Component.literal(
                        target.getGameProfile().name() + " is currently building on space #" + space.info.id
                ).withColor(TextColor.GREEN));
                break;
            }
        }

        return Command.SINGLE_SUCCESS;
    }

}
