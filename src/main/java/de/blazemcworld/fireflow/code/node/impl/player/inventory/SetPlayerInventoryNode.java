package de.blazemcworld.fireflow.code.node.impl.player.inventory;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.*;
import de.blazemcworld.fireflow.code.value.ListValue;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class SetPlayerInventoryNode extends Node {

    public SetPlayerInventoryNode() {
        super("set_player_inventory", "Set Player Inventory", "Changes the entire inventory of a player", Items.WATER_BUCKET);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Input<ListValue<ItemStack>> contents = new Input<>("contents", "Contents", ListType.of(ItemType.INSTANCE));
        Input<String> behaviour = new Input<>("behaviour", "Behaviour", StringType.INSTANCE)
                .options("Clear", "Merge");
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            player.getValue(ctx).tryUse(ctx, p -> {
                boolean clearInv = behaviour.getValue(ctx).equals("Clear");
                if (clearInv) p.getInventory().clear();

                ListValue<ItemStack> items = contents.getValue(ctx);
                int stop = Math.min(items.size(), p.getInventory().size());
                for (int slot = 0; slot < stop; slot++) {
                    ItemStack replacement = items.get(slot);
                    if (!clearInv && replacement.isEmpty()) continue;
                    p.getInventory().setStack(slot, replacement);
                }
            });
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetPlayerInventoryNode();
    }

}


