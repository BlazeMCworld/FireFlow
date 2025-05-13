package de.blazemcworld.fireflow.code.node.impl.item;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.NumberType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class SetItemCountNode extends Node {

    public SetItemCountNode() {
        super("set_item_count", "Set Item Count", "Sets the count of an item", Items.BUNDLE);

        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);
        Input<Double> count = new Input<>("count", "Count", NumberType.INSTANCE);
        Output<ItemStack> updated = new Output<>("updated", "Updated", ItemType.INSTANCE);

        updated.valueFrom((ctx) -> item.getValue(ctx).copyWithCount(count.getValue(ctx).intValue()));
    }

    @Override
    public Node copy() {
        return new SetItemCountNode();
    }

}
