package de.blazemcworld.fireflow.code.node.impl.item;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.ItemType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class IsItemUnbreakableNode extends Node {

    public IsItemUnbreakableNode() {
        super("is_item_unbreakable", "Is Item Unbreakable", "Checks if an item is unbreakable or not.", Items.IRON_HOE);

        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);
        Output<Boolean> unbreakable = new Output<>("unbreakable", "Unbreakable", ConditionType.INSTANCE);

        unbreakable.valueFrom((ctx) -> item.getValue(ctx).has(DataComponents.UNBREAKABLE));
    }

    @Override
    public Node copy() {
        return new IsItemUnbreakableNode();
    }
}
