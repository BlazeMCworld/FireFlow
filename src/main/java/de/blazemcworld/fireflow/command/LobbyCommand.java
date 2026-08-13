package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import de.blazemcworld.fireflow.space.Lobby;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;

public class LobbyCommand {

    public static void register(CommandDispatcher<CommandSourceStack> cd) {
        register(cd, "spawn");
        register(cd, "lobby");
    }

    private static void register(CommandDispatcher<CommandSourceStack> cd, String alias) {
        cd.register(Commands.literal(alias)
                .executes(ctx -> {
                    ServerPlayer player = CommandHelper.getPlayer(ctx.getSource());
                    if (player == null) return Command.SINGLE_SUCCESS;

                    if (player.level() == Lobby.level) {
                        player.sendSystemMessage(Component.literal("You are already in the lobby!").withColor(TextColor.RED));
                        return Command.SINGLE_SUCCESS;
                    }

                    ModeManager.move(player, ModeManager.Mode.LOBBY, null);
                    return Command.SINGLE_SUCCESS;
                })
        );
    }

}
