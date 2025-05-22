package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.blazemcworld.fireflow.code.EditOrigin;
import de.blazemcworld.fireflow.space.Space;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class DebugCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("debug")
                .then(CommandManager.argument("id", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayerEntity player = CommandHelper.getPlayer(ctx.getSource());
                            Space space = CommandHelper.getSpace(player);
                            if (!CommandHelper.isDeveloperOrOwner(player, space)) return Command.SINGLE_SUCCESS;

                            space.evaluator.nextTick(() -> {
                                space.evaluator.triggerDebug(StringArgumentType.getString(ctx, "id"), EditOrigin.ofPlayer(player));
                            });
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }

}
