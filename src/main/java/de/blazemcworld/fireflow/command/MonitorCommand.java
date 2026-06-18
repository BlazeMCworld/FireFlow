package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.blazemcworld.fireflow.messages.Messages;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MonitorCommand {

    private static final Map<ServerPlayerEntity, Space> monitors = new ConcurrentHashMap<>();

    public static void attach(LiteralArgumentBuilder<ServerCommandSource> node) {
        node.then(CommandManager.literal("monitor")
                .executes(ctx -> {
                    ServerPlayerEntity player = CommandHelper.getPlayer(ctx.getSource());

                    if (player != null && monitors.containsKey(player)) {
                        monitors.remove(player);
                        Messages.sendMessage("Stopped monitoring!", Messages.INFO, player);
                        return Command.SINGLE_SUCCESS;
                    }

                    Space space = CommandHelper.getSpace(player);
                    if (!CommandHelper.isDeveloperOrOwner(player, space)) return Command.SINGLE_SUCCESS;

                    monitors.put(player, space);
                    Messages.sendMessage("Now monitoring space #" + space.info.id, Messages.INFO, player);
                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    static {
        ServerTickEvents.END_WORLD_TICK.register((w) -> {
            for (ServerPlayerEntity p : w.getPlayers()) {
                Space space = monitors.get(p);
                if (space == null) continue;

                if (SpaceManager.getSpaceForPlayer(p) != space || !space.info.isOwnerOrDeveloper(p.getUuid())) {
                    monitors.remove(p);
                    return;
                }

                int percent = Math.clamp(space.playWorld.cpuMs() / 10, 0, 100);

                int red = (int) (percent * 2.55);
                int green = 255 - red;
                int color = (green + (red * 256)) * 256;

                int bars = (int) (percent * 0.8);

                p.sendMessage(
                        Text.literal("CPU ").withColor(color)
                                .append(Text.literal("[").formatted(Formatting.WHITE))
                                .append(Text.literal("|".repeat(bars)).withColor(color))
                                .append(Text.literal("|".repeat(80 - bars)).formatted(Formatting.GRAY))
                                .append(Text.literal("]").formatted(Formatting.WHITE))
                                .append(Text.literal(" " + percent + "%").withColor(color)),
                        true
                );
            }
        });
    }

}
