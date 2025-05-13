package de.blazemcworld.fireflow.code.node.impl.player.effect;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;

public class SetExperienceLevelNode extends Node {
    public SetExperienceLevelNode() {
        super("set_experience_level", "Set Experience Level", "Sets the experience level of the player", Items.ENCHANTING_TABLE);
        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<Double> level = new Input<>("level", "Level", NumberType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);
        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> p.setExperienceLevel(level.getValue(ctx).intValue()));
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetExperienceLevelNode();
    }
}