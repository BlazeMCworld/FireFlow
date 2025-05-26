package de.blazemcworld.fireflow.code.node.impl.player.movement;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;

public class PlayerIsFlyingNode extends Node {
    public PlayerIsFlyingNode() {
        super("player_is_flying", "Player Is Flying", "Checks if the player is flying", Items.FEATHER);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<Boolean> flying = new Output<>("flying", "Flying", ConditionType.INSTANCE);

        flying.valueFrom(ctx -> player.getValue(ctx).tryGet(ctx, p -> p.getAbilities().flying, false));
    }

    @Override
    public Node copy() {
        return new PlayerIsFlyingNode();
    }
}
