package de.blazemcworld.fireflow.code.node.impl.item;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.NumberType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class GetItemCountNode extends Node {

    public GetItemCountNode() {
        super("get_item_count", "Get Item Count", "Gets the stack size of an item value", Items.CYAN_BUNDLE);

        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);
        Output<Double> count = new Output<>("count", "Count", NumberType.INSTANCE);

        count.valueFrom((ctx) -> (double) item.getValue(ctx).getCount());
    }

    @Override
    public Node copy() {
        return new GetItemCountNode();
    }

}
