package de.blazemcworld.fireflow.code.node.impl.player.info;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;

public class IsPlayerInvulnerableNode extends Node {
    public IsPlayerInvulnerableNode() {
        super("is_player_invulnerable", Material.IRON_SWORD);
        Input<PlayerValue> player = new Input<>("player", PlayerType.INSTANCE);
        Output<Boolean> invulnerable = new Output<>("invulnerable", ConditionType.INSTANCE);

        invulnerable.valueFrom(ctx -> player.getValue(ctx).tryGet(ctx, Player::isInvulnerable, false));
    }

    @Override
    public Node copy() {
        return new IsPlayerInvulnerableNode();
    }
}
