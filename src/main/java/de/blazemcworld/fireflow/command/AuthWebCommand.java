package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.blazemcworld.fireflow.space.Space;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class AuthWebCommand {

    public static void register(CommandDispatcher<ServerCommandSource> cd) {
        cd.register(CommandManager.literal("authorize-web")
                .then(CommandManager.argument("id", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayerEntity player = CommandHelper.getPlayer(ctx.getSource());
                            Space space = CommandHelper.getSpace(player);
                            if (!CommandHelper.isDeveloperOrOwner(player, space)) return Command.SINGLE_SUCCESS;

                            space.editor.authorizeWeb(StringArgumentType.getString(ctx, "id"), player);
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }

}
