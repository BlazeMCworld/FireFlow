package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.blazemcworld.fireflow.code.EditOrigin;
import de.blazemcworld.fireflow.messages.Messages;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AddNodeCommand {

    public static void register(CommandDispatcher<ServerCommandSource> cd) {
        register(cd, "add", false);
        register(cd, "add?", true);
    }

    private static void register(CommandDispatcher<ServerCommandSource> cd, String id, boolean flag) {
        cd.register(CommandManager.literal(id)
                .then(CommandManager.argument("node", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayerEntity player = CommandHelper.getPlayer(ctx.getSource());
                            Space space = CommandHelper.getSpace(player);
                            if (space == null) return Command.SINGLE_SUCCESS;

                            if (ModeManager.getFor(player) != ModeManager.Mode.CODE) {
                                Messages.sendMessage("You must be in code mode to do that!", Messages.ERROR, player);
                                return Command.SINGLE_SUCCESS;
                            }

                            space.editor.addNode(EditOrigin.ofPlayer(player), StringArgumentType.getString(ctx, "node"), flag);

                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }


}
