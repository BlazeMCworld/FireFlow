package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceInfo;
import de.blazemcworld.fireflow.space.SpaceManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ShowLagCommand {

    public static void register(CommandDispatcher<CommandSourceStack> cd) {
        cd.register(Commands.literal("showlag")
                .executes(ctx -> {
                    List<Pair<SpaceInfo, Integer>> cpuUsages = new ArrayList<>();
                    for (Space space : SpaceManager.getLoadedSpaces()) {
                        int percent = Math.clamp(space.playLevel.cpuMs() / 10, 0, 100);
                        if (percent < 10) continue;
                        cpuUsages.add(new Pair<>(space.info, percent));
                    }

                    if (cpuUsages.isEmpty()) {
                        ctx.getSource().sendSuccess(() -> Component.literal("All should be good!").withColor(TextColor.GREEN), false);
                        return Command.SINGLE_SUCCESS;
                    }

                    ctx.getSource().sendSuccess(() -> Component.literal("Found " + cpuUsages.size() + " spaces which might be affecting server performance.").withColor(TextColor.DARK_AQUA), false);

                    Style style = Style.EMPTY
                            .withColor(ChatFormatting.DARK_AQUA)
                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("CPU usage shown in allowance per space.")
                                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))));

                    cpuUsages.sort(Comparator.comparingInt(p -> -p.getB()));
                    for (Pair<SpaceInfo, Integer> entry : cpuUsages) {
                        ctx.getSource().sendSuccess(() -> Component.literal("Space #" + entry.getA().id + ": " + entry.getB() + "%").setStyle(style), false);
                    }
                    return Command.SINGLE_SUCCESS;
                }));
    }
}
