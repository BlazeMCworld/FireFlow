package de.blazemcworld.fireflow.code.node.impl.player.info;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.PlayerType;
import de.blazemcworld.fireflow.code.value.ListValue;
import de.blazemcworld.fireflow.code.value.PlayerValue;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;

public class GetPlayerInventoryNode extends Node {

    public GetPlayerInventoryNode() {
        super("get_player_inventory", "Get Player Inventory", "Gets a list of all items in the player's main inventory. Does not include the offhand, armor or crafting slots.", Items.BARREL);

        Input<PlayerValue> player = new Input<>("player", "Player", PlayerType.INSTANCE);
        Output<ListValue<ItemStack>> main = new Output<>("main", "Main", ListType.of(ItemType.INSTANCE));

        main.valueFrom((ctx) -> player.getValue(ctx).tryGet(ctx, p ->
                new ListValue<>(ItemType.INSTANCE, new ArrayList<>(p.getInventory().getMainStacks())), new ListValue<>(ItemType.INSTANCE)
        ));
    }

    @Override
    public Node copy() {
        return new GetPlayerInventoryNode();
    }

}
