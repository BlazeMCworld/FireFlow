package de.blazemcworld.fireflow.command;

import de.blazemcworld.fireflow.space.Space;
import de.blazemcworld.fireflow.space.SpaceInfo;
import de.blazemcworld.fireflow.space.SpaceManager;
import de.blazemcworld.fireflow.util.Transfer;
import de.blazemcworld.fireflow.util.Translations;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.entity.Player;

public class CodeCommand extends Command {

    public CodeCommand() {
        super("code", "dev");

        setDefaultExecutor((sender, ctx) -> {
            if (sender instanceof Player player) {
                Space space = SpaceManager.getSpaceForPlayer(player);
                if (space == null) {
                    sender.sendMessage(Component.text(Translations.get("error.needs.space")).color(NamedTextColor.RED));
                    return;
                }

                if (space.code == player.getInstance()) {
                    sender.sendMessage(Component.text(Translations.get("error.already.coding")).color(NamedTextColor.RED));
                    return;
                }

                if (!space.isOwnerOrDeveloper(player)) {
                    sender.sendMessage(Component.text(Translations.get("error.needs.permission")).color(NamedTextColor.RED));
                    return;
                }

                Transfer.move(player, space.code);
            } else {
                sender.sendMessage(Component.text(Translations.get("error.needs.player")).color(NamedTextColor.RED));
            }
        });

        addSyntax((sender, ctx) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text(Translations.get("error.needs.player")).color(NamedTextColor.RED));
                return;
            }
            SpaceInfo info = SpaceManager.info.get(ctx.<Integer>get("id"));
            if (info == null) {
                sender.sendMessage(Component.text(Translations.get("error.invalid.space")).color(NamedTextColor.RED));
                return;
            }

            if (!info.owner.equals(player.getUuid()) && !info.developers.contains(player.getUuid())) {
                sender.sendMessage(Component.text(Translations.get("error.needs.permission")).color(NamedTextColor.RED));
                return;
            }

            Space space = SpaceManager.getOrLoadSpace(info);

            if (space.code == player.getInstance()) {
                sender.sendMessage(Component.text(Translations.get("error.already.coding")).color(NamedTextColor.RED));
                return;
            }

            Transfer.move(player, space.code);
        }, new ArgumentInteger("id"));
    }

}
