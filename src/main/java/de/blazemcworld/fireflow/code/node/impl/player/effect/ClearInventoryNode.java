package de.blazemcworld.fireflow.code.node.impl.player.effect;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.Items;

public class ClearInventoryNode extends Node {
    public ClearInventoryNode() {
        super("clear_inventory", "Clear Inventory", "Clears the player's inventory", Items.FILLED_MAP);
        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> p.getInventory().clear());
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new ClearInventoryNode();
    }
}