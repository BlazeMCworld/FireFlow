package de.blazemcworld.fireflow.code.node.impl.player.statistic;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;

public class GetPlayerFoodNode extends Node {
    public GetPlayerFoodNode() {
        super("get_player_food", "Get Player Food", "Gets the food level of the player", Items.COOKED_BEEF);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<Double> food = new Output<>("food", "Food", NumberType.INSTANCE);

        food.valueFrom(ctx -> player.getValue(ctx).tryGet(ctx, p -> (double) p.getHungerManager().getFoodLevel(), 0.0));
    }

    @Override
    public Node copy() {
        return new GetPlayerFoodNode();
    }
}
