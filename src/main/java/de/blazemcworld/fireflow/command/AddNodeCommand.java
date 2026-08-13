package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.blazemcworld.fireflow.code.EditOrigin;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;

public class AddNodeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> cd) {
        register(cd, "add", false);
        register(cd, "add?", true);
    }

    private static void register(CommandDispatcher<CommandSourceStack> cd, String id, boolean flag) {
        cd.register(Commands.literal(id)
                .then(Commands.argument("node", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayer player = CommandHelper.getPlayer(ctx.getSource());
                            Space space = CommandHelper.getSpace(player);
                            if (space == null) return Command.SINGLE_SUCCESS;

                            if (ModeManager.getFor(player) != ModeManager.Mode.CODE) {
                                player.sendSystemMessage(Component.literal("You must be in code mode to do that!").withColor(TextColor.RED));
                                return Command.SINGLE_SUCCESS;
                            }

                            space.editor.addNode(EditOrigin.ofPlayer(player), StringArgumentType.getString(ctx, "node"), flag);

                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }


}
