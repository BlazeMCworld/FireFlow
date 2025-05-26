package de.blazemcworld.fireflow.code.node.impl.player.movement;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;

public class IsPlayerSneakingNode extends Node {
    public IsPlayerSneakingNode() {
        super("is_player_sneaking", "Is Player Sneaking", "Checks if the player is sneaking", Items.LEATHER_BOOTS);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<Boolean> sneaking = new Output<>("sneaking", "Sneaking", ConditionType.INSTANCE);

        sneaking.valueFrom(ctx -> player.getValue(ctx).tryGet(ctx, p -> p.isSneaking(), false));
    }

    @Override
    public Node copy() {
        return new IsPlayerSneakingNode();
    }
}