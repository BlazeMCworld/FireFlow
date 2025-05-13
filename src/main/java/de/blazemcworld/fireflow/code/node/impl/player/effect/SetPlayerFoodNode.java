package de.blazemcworld.fireflow.code.node.impl.player.effect;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;

public class SetPlayerFoodNode extends Node {
    public SetPlayerFoodNode() {
        super("set_player_food", "Set Player Food", "Sets the food level of a player", Items.COOKED_BEEF);
        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<Double> food = new Input<>("food", "Food", NumberType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);
        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> p.getHungerManager().setFoodLevel(food.getValue(ctx).intValue()));
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetPlayerFoodNode();
    }
}