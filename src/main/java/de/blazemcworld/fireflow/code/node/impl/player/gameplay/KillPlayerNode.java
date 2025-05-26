package de.blazemcworld.fireflow.code.node.impl.player.gameplay;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;

public class KillPlayerNode extends Node {
    public KillPlayerNode() {
        super("kill_player", "Kill Player", "Kills the player", Items.NETHERITE_SWORD);
        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);
        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> p.kill(ctx.evaluator.world));
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new KillPlayerNode();
    }
}
