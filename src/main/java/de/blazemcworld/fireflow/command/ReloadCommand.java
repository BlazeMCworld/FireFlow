package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import de.blazemcworld.fireflow.space.Space;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ReloadCommand {

    public static void register(CommandDispatcher<ServerCommandSource> cd) {
        cd.register(CommandManager.literal("reload")
                .executes(ctx -> {
                    ServerPlayerEntity player = CommandHelper.getPlayer(ctx.getSource());
                    Space space = CommandHelper.getSpace(player);
                    if (space == null) return Command.SINGLE_SUCCESS;

                    if (!space.info.isOwnerOrDeveloper(player.getUuid())) {
                        player.sendMessage(Text.literal("You are not allowed to do that!").formatted(Formatting.RED));
                        return Command.SINGLE_SUCCESS;
                    }

                    space.reload();
                    return Command.SINGLE_SUCCESS;
                })
        );
    }

}
