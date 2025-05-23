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
                    if (!CommandHelper.isDeveloperOrOwner(player, space)) return Command.SINGLE_SUCCESS;

                    space.reload();
                    player.sendMessage(Text.literal("Reloaded space!").formatted(Formatting.AQUA));
                    return Command.SINGLE_SUCCESS;
                })
                .then(CommandManager.literal("live")
                        .executes(ctx -> {
                            ServerPlayerEntity player = CommandHelper.getPlayer(ctx.getSource());
                            Space space = CommandHelper.getSpace(player);
                            if (!CommandHelper.isDeveloperOrOwner(player, space)) return Command.SINGLE_SUCCESS;

                            space.evaluator.liveReload();
                            player.sendMessage(Text.literal("Live reloaded space!").formatted(Formatting.AQUA));
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }

}
