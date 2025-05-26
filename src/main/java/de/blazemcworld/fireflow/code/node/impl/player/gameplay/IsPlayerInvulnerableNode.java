package de.blazemcworld.fireflow.code.node.impl.player.gameplay;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;

public class IsPlayerInvulnerableNode extends Node {
    public IsPlayerInvulnerableNode() {
        super("is_player_invulnerable", "Is Player Invulnerable", "Checks if the player is invulnerable", Items.IRON_SWORD);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<Boolean> invulnerable = new Output<>("invulnerable", "Invulnerable", ConditionType.INSTANCE);

        invulnerable.valueFrom(ctx -> player.getValue(ctx).tryGet(ctx, p -> p.isInvulnerable(), false));
    }

    @Override
    public Node copy() {
        return new IsPlayerInvulnerableNode();
    }
}
