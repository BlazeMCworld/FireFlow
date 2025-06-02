package de.blazemcworld.fireflow.code.node.impl.player.inventory;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;

public class SetHeldSlotNode extends Node {
    public SetHeldSlotNode() {
        super("set_held_slot", "Set Held Slot", "Sets the held slot of the player", Items.GOLD_INGOT);
        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<Double> slot = new Input<>("slot", "Slot", NumberType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);
        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> p.getInventory().setSelectedSlot((byte) Math.clamp(slot.getValue(ctx).intValue() - 1, 0, 8)));
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetHeldSlotNode();
    }
}

