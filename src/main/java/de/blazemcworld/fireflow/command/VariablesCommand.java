package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.blazemcworld.fireflow.code.VariableStore;
import de.blazemcworld.fireflow.space.Space;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;
import java.util.function.Predicate;

public class VariablesCommand {

    public static void attach(LiteralArgumentBuilder<CommandSourceStack> node) {
        attach(node, "variables");
        attach(node, "vars");
    }

    private static void attach(LiteralArgumentBuilder<CommandSourceStack> node, String alias) {
        node.then(Commands.literal(alias)
                .executes(ctx -> {
                    listVariables(ctx.getSource(), null);
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("filter", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            listVariables(ctx.getSource(), StringArgumentType.getString(ctx, "filter"));
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }

    private static void listVariables(CommandSourceStack source, String query) {
        ServerPlayer player = CommandHelper.getPlayer(source);
        Space space = CommandHelper.getSpace(player);
        if (!CommandHelper.isDeveloperOrOwner(player, space)) return;

        String lowerQuery = query == null ? null : query.toLowerCase();
        Predicate<String> filter = query == null ? s -> true : s -> s.toLowerCase().contains(lowerQuery);

        Set<VariableStore.VarEntry> vars = space.savedVariables.iterator(filter, 50);
        for (VariableStore.VarEntry v : vars) {
            player.sendSystemMessage(Component.literal(v.name()).setStyle(Style.EMPTY.withColor(v.type().color))
                    .append(Component.literal(": ").withColor(TextColor.GRAY))
                    .append(Component.literal(v.type().stringify(v.value(), "display")).withColor(TextColor.WHITE)));
        }

        if (vars.size() >= 50) return;

        for (VariableStore.VarEntry v : space.evaluator.sessionVariables.iterator(filter, 50 - vars.size())) {
            player.sendSystemMessage(Component.literal(v.name()).setStyle(Style.EMPTY.withColor(v.type().color))
                    .append(Component.literal(": ").withColor(TextColor.GRAY))
                    .append(Component.literal(v.type().stringify(v.value(), "display")).withColor(TextColor.WHITE)));
        }
    }

}
