package de.blazemcworld.fireflow.code.node.impl.player.info;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;

public class GetHeldSlotNode extends Node {
    public GetHeldSlotNode() {
        super("get_held_slot", "Get Held Slot", "Gets the held slot of the player", Items.SMOOTH_STONE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<Double> slot = new Output<>("slot", "Slot", NumberType.INSTANCE);

        slot.valueFrom(ctx -> player.getValue(ctx).tryGet(ctx, p -> (double) p.getInventory().getSelectedSlot(), 0.0));
    }

    @Override
    public Node copy() {
        return new GetHeldSlotNode();
    }
}
