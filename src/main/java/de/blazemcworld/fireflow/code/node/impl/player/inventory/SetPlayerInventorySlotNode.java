package de.blazemcworld.fireflow.code.node.impl.player.inventory;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class SetPlayerInventorySlotNode extends Node {

    public SetPlayerInventorySlotNode() {
        super("set_player_inventory_slot", "Set Player Inventory Slot", "Changes a single item slot in the player's inventory.", Items.ITEM_FRAME);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<Double> slot = new Input<>("slot", "Slot", NumberType.INSTANCE);
        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);

        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> {
                int id = slot.getValue(ctx).intValue();
                if (id < 0) return;
                if (id < p.getInventory().size() || PlayerInventory.EQUIPMENT_SLOTS.get(id) != null) {
                    p.getInventory().setStack(id, item.getValue(ctx));
                }
            });
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetPlayerInventorySlotNode();
    }

}
