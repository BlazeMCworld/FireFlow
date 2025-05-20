package de.blazemcworld.fireflow.code.node.impl.player.info;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;

import java.util.UUID;

public class PlayerHasPermissionNode extends Node {

    public PlayerHasPermissionNode() {
        super("player_has_permission", "Player Has Permission", "Checks if a player has specific permissions on the space. Contributor means either builder or developer, and the owner is always included.", Items.DIAMOND);

        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<String> permission = new Input<>("permission", "Permission", StringType.INSTANCE)
                .options("Contributor", "Builder", "Developer", "Owner");
        Output<Boolean> hasPerm = new Output<>("has_permission", "Has Permission", ConditionType.INSTANCE);

        hasPerm.valueFrom(ctx -> {
            UUID uuid = player.getValue(ctx).uuid;
            return switch (permission.getValue(ctx)) {
                case "Contributor" -> ctx.evaluator.space.info.isOwnerOrBuilder(uuid) || ctx.evaluator.space.info.isOwnerOrDeveloper(uuid);
                case "Builder" -> ctx.evaluator.space.info.isOwnerOrBuilder(uuid);
                case "Developer" -> ctx.evaluator.space.info.isOwnerOrDeveloper(uuid);
                case "Owner" -> ctx.evaluator.space.info.owner.equals(uuid);
                default -> false;
            };
        });
    }

    @Override
    public Node copy() {
        return new PlayerHasPermissionNode();
    }
}
