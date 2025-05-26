package de.blazemcworld.fireflow.code.node.impl.player.movement;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;

public class PlayerCanFlyNode extends Node {
    public PlayerCanFlyNode() {
        super("player_can_fly", "Player Can Fly", "Checks if the player can fly", Items.WHITE_WOOL);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<Boolean> allowed = new Output<>("allowed", "Allowed", ConditionType.INSTANCE);

        allowed.valueFrom(ctx -> player.getValue(ctx).tryGet(ctx, p -> p.getAbilities().allowFlying, false));
    }

    @Override
    public Node copy() {
        return new PlayerCanFlyNode();
    }
}
