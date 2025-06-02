package de.blazemcworld.fireflow.code.node.impl.player.movement;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;

public class SetAllowFlyingNode extends Node {
    public SetAllowFlyingNode() {
        super("set_allow_flying", "Set Allow Flying", "Allows or disallows the player to fly", Items.FEATHER);
        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<Boolean> allow = new Input<>("allow", "Allow", ConditionType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);
        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> {
                boolean newValue = allow.getValue(ctx);
                p.getAbilities().allowFlying = newValue;
                if (!newValue) p.getAbilities().flying = false;
                p.sendAbilitiesUpdate();
            });
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetAllowFlyingNode();
    }
}
