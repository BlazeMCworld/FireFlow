package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.blazemcworld.fireflow.space.Space;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class AuthWebCommand {

    public static void register(CommandDispatcher<CommandSourceStack> cd) {
        cd.register(Commands.literal("authorize-web")
                .then(Commands.argument("id", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayer player = CommandHelper.getPlayer(ctx.getSource());
                            Space space = CommandHelper.getSpace(player);
                            if (!CommandHelper.isDeveloperOrOwner(player, space)) return Command.SINGLE_SUCCESS;

                            space.editor.authorizeWeb(StringArgumentType.getString(ctx, "id"), player);
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }

}
