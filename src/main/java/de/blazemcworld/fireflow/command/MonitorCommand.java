package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MonitorCommand {

    private static final Map<ServerPlayer, Space> monitors = new ConcurrentHashMap<>();

    public static void attach(LiteralArgumentBuilder<CommandSourceStack> node) {
        node.then(Commands.literal("monitor")
                .executes(ctx -> {
                    ServerPlayer player = CommandHelper.getPlayer(ctx.getSource());

                    if (player != null && monitors.containsKey(player)) {
                        monitors.remove(player);
                        player.sendSystemMessage(Component.literal("Stopped monitoring!").withColor(TextColor.AQUA));
                        return Command.SINGLE_SUCCESS;
                    }

                    Space space = CommandHelper.getSpace(player);
                    if (!CommandHelper.isDeveloperOrOwner(player, space)) return Command.SINGLE_SUCCESS;

                    monitors.put(player, space);
                    player.sendSystemMessage(Component.literal("Now monitoring space #" + space.info.id).withColor(TextColor.AQUA));
                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    static {
        ServerTickEvents.END_LEVEL_TICK.register((w) -> {
            for (ServerPlayer p : w.players()) {
                Space space = monitors.get(p);
                if (space == null) continue;

                if (SpaceManager.getSpaceForPlayer(p) != space || !space.info.isOwnerOrDeveloper(p.getUUID())) {
                    monitors.remove(p);
                    return;
                }

                int percent = Math.clamp(space.playLevel.cpuMs() / 10, 0, 100);

                int red = (int) (percent * 2.55);
                int green = 255 - red;
                int color = (green + (red * 256)) * 256;

                int bars = (int) (percent * 0.8);

                p.sendSystemMessage(
                        Component.literal("CPU ").withColor(color)
                                .append(Component.literal("[").withColor(TextColor.WHITE))
                                .append(Component.literal("|".repeat(bars)).withColor(color))
                                .append(Component.literal("|".repeat(80 - bars)).withColor(TextColor.GRAY))
                                .append(Component.literal("]").withColor(TextColor.WHITE))
                                .append(Component.literal(" " + percent + "%").withColor(color)),
                        true
                );
            }
        });
    }

}
