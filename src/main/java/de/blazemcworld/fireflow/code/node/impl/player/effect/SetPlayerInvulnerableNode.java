package de.blazemcworld.fireflow.code.node.impl.player.effect;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;

public class SetPlayerInvulnerableNode extends Node {
    public SetPlayerInvulnerableNode() {
        super("set_player_invulnerable", "Set Player Invulnerable", "Sets the invulnerability state of the player", Items.SHIELD);
        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<Boolean> state = new Input<>("state", "State", ConditionType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);
        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> p.setInvulnerable(state.getValue(ctx)));
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetPlayerInvulnerableNode();
    }
}
