package de.blazemcworld.fireflow.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.blazemcworld.fireflow.code.VariableStore;
import de.blazemcworld.fireflow.space.Space;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Set;
import java.util.function.Predicate;

public class VariablesCommand {

    public static void attach(LiteralArgumentBuilder<ServerCommandSource> node) {
        attach(node, "variables");
        attach(node, "vars");
    }

    private static void attach(LiteralArgumentBuilder<ServerCommandSource> node, String alias) {
        node.then(CommandManager.literal(alias)
                .executes(ctx -> {
                    listVariables(ctx.getSource(), null);
                    return Command.SINGLE_SUCCESS;
                })
                .then(CommandManager.argument("filter", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            listVariables(ctx.getSource(), StringArgumentType.getString(ctx, "filter"));
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }

    private static void listVariables(ServerCommandSource source, String query) {
        ServerPlayerEntity player = CommandHelper.getPlayer(source);
        Space space = CommandHelper.getSpace(player);
        if (!CommandHelper.isDeveloperOrOwner(player, space)) return;

        String lowerQuery = query == null ? null : query.toLowerCase();
        Predicate<String> filter = query == null ? s -> true : s -> s.toLowerCase().contains(lowerQuery);

        Set<VariableStore.VarEntry> vars = space.savedVariables.iterator(filter, 50);
        for (VariableStore.VarEntry v : vars) {
            player.sendMessage(Text.literal(v.name()).setStyle(Style.EMPTY.withColor(v.type().color))
                    .append(Text.literal(": ").formatted(Formatting.GRAY))
                    .append(Text.literal(v.type().stringify(v.value(), "display")).formatted(Formatting.WHITE)));
        }

        if (vars.size() >= 50) return;

        for (VariableStore.VarEntry v : space.evaluator.sessionVariables.iterator(filter, 50 - vars.size())) {
            player.sendMessage(Text.literal(v.name()).setStyle(Style.EMPTY.withColor(v.type().color))
                    .append(Text.literal(": ").formatted(Formatting.GRAY))
                    .append(Text.literal(v.type().stringify(v.value(), "display")).formatted(Formatting.WHITE)));
        }
    }

}
