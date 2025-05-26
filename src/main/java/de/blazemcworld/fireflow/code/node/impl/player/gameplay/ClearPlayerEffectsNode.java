package de.blazemcworld.fireflow.code.node.impl.player.gameplay;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;

public class ClearPlayerEffectsNode extends Node {

    public ClearPlayerEffectsNode() {
        super("clear_player_effects", "Clear Player Effects", "Removes all potion effects from a player.", Items.MILK_BUCKET);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, LivingEntity::clearStatusEffects);
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new ClearPlayerEffectsNode();
    }
}
