package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import de.blazemcworld.fireflow.messages.Messages;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceInfo;
import de.blazemcworld.fireflow.space.SpaceManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ShowLagCommand {

    public static void register(CommandDispatcher<ServerCommandSource> cd) {
        cd.register(CommandManager.literal("showlag")
                .executes(ctx -> {
                    List<Pair<SpaceInfo, Integer>> cpuUsages = new ArrayList<>();
                    for (Space space : SpaceManager.getLoadedSpaces()) {
                        int percent = Math.clamp(space.playWorld.cpuMs() / 10, 0, 100);
                        if (percent < 10) continue;
                        cpuUsages.add(new Pair<>(space.info, percent));
                    }

                    if (cpuUsages.isEmpty()) {
                        Messages.sendMessage("All should be good!", Messages.INFO, ctx.getSource());
                        return Command.SINGLE_SUCCESS;
                    }

                    Messages.sendMessage("Found " + cpuUsages.size() + " spaces which might be affecting server performance.", Messages.INFO, ctx.getSource());

                    cpuUsages.sort(Comparator.comparingInt(p -> -p.getRight()));
                    for (Pair<SpaceInfo, Integer> entry : cpuUsages) {
                        Messages.sendMessage(
                                "<hover:show_text:\"<default>CPU usage shown in allowance per space\">Space #" + entry.getLeft().id + ": " + entry.getRight() + "%",
                                Messages.FOLLOWUP, ctx.getSource()
                        );
                    }
                    return Command.SINGLE_SUCCESS;
                }));
    }
}
