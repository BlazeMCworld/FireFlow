package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceInfo;
import de.blazemcworld.fireflow.space.SpaceManager;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;

public class CodeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> cd) {
        register(cd, "code");
        register(cd, "dev");
    }

    private static void register(CommandDispatcher<CommandSourceStack> cd, String alias) {
        cd.register(Commands.literal(alias)
                .executes(ctx -> {
                    ServerPlayer player = CommandHelper.getPlayer(ctx.getSource());
                    Space space = CommandHelper.getSpace(player);
                    if (!CommandHelper.isDeveloperOrOwner(player, space)) return Command.SINGLE_SUCCESS;

                    ModeManager.move(player, ModeManager.Mode.CODE, space);
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("id", IntegerArgumentType.integer())
                        .executes(ctx -> {
                            ServerPlayer player = CommandHelper.getPlayer(ctx.getSource());
                            if (player == null) return Command.SINGLE_SUCCESS;

                            int id = IntegerArgumentType.getInteger(ctx, "id");
                            SpaceInfo info = SpaceManager.getInfo(id);
                            if (info == null) {
                                player.sendSystemMessage(Component.literal("Could not find space with id " + id + "!").withColor(TextColor.RED));
                                return Command.SINGLE_SUCCESS;
                            }

                            if (!info.isOwnerOrDeveloper(player.getUUID())) {
                                player.sendSystemMessage(Component.literal("You are not allowed to do that!").withColor(TextColor.RED));
                                return Command.SINGLE_SUCCESS;
                            }

                            ModeManager.move(player, ModeManager.Mode.CODE, SpaceManager.getOrLoadSpace(info));
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }

}
