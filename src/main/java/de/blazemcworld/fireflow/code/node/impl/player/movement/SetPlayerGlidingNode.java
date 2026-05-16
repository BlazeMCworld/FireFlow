package de.blazemcworld.fireflow.code.node.impl.player.movement;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;

public class SetPlayerGlidingNode extends Node {
    public SetPlayerGlidingNode() {
        super("set_player_gliding", "Set Player Gliding", "Sets the gliding state of the player", Items.ELYTRA);
        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<Boolean> allow = new Input<>("allow", "Allow", ConditionType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);
        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> {
                if (allow.getValue(ctx)) p.startGliding();
                else p.stopGliding();
            });
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetPlayerGlidingNode();
    }
}
