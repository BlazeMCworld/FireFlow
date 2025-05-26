package de.blazemcworld.fireflow.code.node.impl.player.meta;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import de.blazemcworld.fireflow.util.ModeManager;
import net.minecraft.item.Items;

public class KickPlayerNode extends Node {

    public KickPlayerNode() {
        super("kick_player", "Kick Player", "Forcefully makes a player leave the current space.", Items.BARRIER);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> {
                if (ctx.evaluator.space.info.isOwnerOrDeveloper(p.getUuid())) {
                    ModeManager.move(p, ModeManager.Mode.CODE, ctx.evaluator.space);
                } else {
                    ModeManager.move(p, ModeManager.Mode.LOBBY, null);
                }
            });
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new KickPlayerNode();
    }
}
