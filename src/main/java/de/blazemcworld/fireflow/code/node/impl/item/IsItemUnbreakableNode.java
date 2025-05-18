package de.blazemcworld.fireflow.code.node.impl.item;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.ItemType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class IsItemUnbreakableNode extends Node {

    public IsItemUnbreakableNode() {
        super("set_item_unbreakable", "Set Item Unbreakable", "Changes whether an item is unbreakable or not.", Items.IRON_HOE);

        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);
        Output<Boolean> unbreakable = new Output<>("unbreakable", "Unbreakable", ConditionType.INSTANCE);

        unbreakable.valueFrom((ctx) -> item.getValue(ctx).contains(DataComponentTypes.UNBREAKABLE));
    }

    @Override
    public Node copy() {
        return new IsItemUnbreakableNode();
    }
}
