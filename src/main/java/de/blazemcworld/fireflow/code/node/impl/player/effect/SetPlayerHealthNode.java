package de.blazemcworld.fireflow.code.node.impl.player.effect;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;

public class SetPlayerHealthNode extends Node {
    public SetPlayerHealthNode() {
        super("set_player_health", "Set Player Health", "Sets the health points of a player", Items.RED_DYE);
        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<Double> health = new Input<>("health", "Health", NumberType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);
        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> p.setHealth(health.getValue(ctx).intValue()));
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetPlayerHealthNode();
    }
}
