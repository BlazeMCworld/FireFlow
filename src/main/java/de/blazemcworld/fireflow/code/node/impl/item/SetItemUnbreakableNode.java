package de.blazemcworld.fireflow.code.node.impl.item;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.ItemType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Unit;

public class SetItemUnbreakableNode extends Node {

    public SetItemUnbreakableNode() {
        super("set_item_unbreakable", "Set Item Unbreakable", "Changes whether an item is unbreakable or not.", Items.NETHERITE_HOE);

        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);
        Input<Boolean> unbreakable = new Input<>("unbreakable", "Unbreakable", ConditionType.INSTANCE);
        Output<ItemStack> updated = new Output<>("updated", "Updated", ItemType.INSTANCE);

        updated.valueFrom((ctx) -> {
            ItemStack i = item.getValue(ctx);
            if (unbreakable.getValue(ctx)) {
                i.set(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE);
            } else {
                i.remove(DataComponentTypes.UNBREAKABLE);
            }
            return i;
        });
    }

    @Override
    public Node copy() {
        return new SetItemUnbreakableNode();
    }
}
