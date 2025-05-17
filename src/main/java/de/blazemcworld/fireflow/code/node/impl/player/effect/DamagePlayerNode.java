package de.blazemcworld.fireflow.code.node.impl.player.effect;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;

public class DamagePlayerNode extends Node {

    public DamagePlayerNode() {
        super("damage_player", "Damage Player", "Applies damage to a player.", Items.GOLDEN_AXE);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<Double> amount = new Input<>("amount", "Amount", NumberType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal(ctx -> {
            player.getValue(ctx).tryUse(ctx, p -> {
                p.damage(ctx.evaluator.world, ctx.evaluator.world.getDamageSources().magic(), amount.getValue(ctx).floatValue());
            });

            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new DamagePlayerNode();
    }
}
