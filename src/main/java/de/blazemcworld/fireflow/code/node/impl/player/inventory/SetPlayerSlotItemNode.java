package de.blazemcworld.fireflow.code.node.impl.player.inventory;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class SetPlayerSlotItemNode extends Node {

    public SetPlayerSlotItemNode() {
        super("set_player_slot_item", "Set Player Slot Item", "Changes the item in a player's inventory slot", Items.SMOOTH_STONE);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<Double> slot = new Input<>("slot", "Slot", NumberType.INSTANCE);
        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> {
                int s = slot.getValue(ctx).intValue();
                if (s < 0 || s >= p.getInventory().size()) return;
                p.getInventory().setStack(s, item.getValue(ctx));
            });
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetPlayerSlotItemNode();
    }

}

