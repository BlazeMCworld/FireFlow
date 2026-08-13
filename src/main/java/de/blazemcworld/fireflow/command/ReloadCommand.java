package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import de.blazemcworld.fireflow.space.Space;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;

public class ReloadCommand {

    public static void register(CommandDispatcher<CommandSourceStack> cd) {
        cd.register(Commands.literal("reload")
                .executes(ctx -> {
                    ServerPlayer player = CommandHelper.getPlayer(ctx.getSource());
                    Space space = CommandHelper.getSpace(player);
                    if (!CommandHelper.isDeveloperOrOwner(player, space)) return Command.SINGLE_SUCCESS;

                    space.reload();
                    player.sendSystemMessage(Component.literal("Reloaded space!").withColor(TextColor.AQUA));
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.literal("live")
                        .executes(ctx -> {
                            ServerPlayer player = CommandHelper.getPlayer(ctx.getSource());
                            Space space = CommandHelper.getSpace(player);
                            if (!CommandHelper.isDeveloperOrOwner(player, space)) return Command.SINGLE_SUCCESS;

                            space.evaluator.liveReload();
                            player.sendSystemMessage(Component.literal("Live reloaded space!").withColor(TextColor.AQUA));
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }

}
