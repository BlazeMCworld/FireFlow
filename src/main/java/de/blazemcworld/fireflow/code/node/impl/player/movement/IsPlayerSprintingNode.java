package de.blazemcworld.fireflow.code.node.impl.player.movement;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;

public class IsPlayerSprintingNode extends Node {
    public IsPlayerSprintingNode() {
        super("is_player_sprinting", "Is Player Sprinting", "Checks if the player is sprinting", Items.LEATHER_LEGGINGS);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<Boolean> sprinting = new Output<>("sprinting", "Sprinting", ConditionType.INSTANCE);

        sprinting.valueFrom(ctx -> player.getValue(ctx).tryGet(ctx, Entity::isSprinting, false));
    }

    @Override
    public Node copy() {
        return new IsPlayerSprintingNode();
    }
}