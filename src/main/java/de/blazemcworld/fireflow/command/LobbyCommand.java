package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import de.blazemcworld.fireflow.space.Lobby;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class LobbyCommand {

    public static void register(CommandDispatcher<ServerCommandSource> cd) {
        register(cd, "spawn");
        register(cd, "lobby");
    }

    private static void register(CommandDispatcher<ServerCommandSource> cd, String alias) {
        cd.register(CommandManager.literal(alias)
                .executes(ctx -> {
                    ServerPlayerEntity player = CommandHelper.getPlayer(ctx.getSource());

                    if (player.getServerWorld() == Lobby.world) {
                        player.sendMessage(Text.literal("You are already in the lobby!").formatted(Formatting.RED));
                        return Command.SINGLE_SUCCESS;
                    }

                    ModeManager.move(player, ModeManager.Mode.LOBBY, null);
                    return Command.SINGLE_SUCCESS;
                })
        );
    }

}
