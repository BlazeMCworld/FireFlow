package de.blazemcworld.fireflow.code.node.impl.item;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.ItemType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SetItemUnbreakableNode extends Node {

    public SetItemUnbreakableNode() {
        super("set_item_unbreakable", "Set Item Unbreakable", "Changes whether an item is unbreakable or not.", Items.NETHERITE_HOE);

        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);
        Input<Boolean> unbreakable = new Input<>("unbreakable", "Unbreakable", ConditionType.INSTANCE);
        Output<ItemStack> updated = new Output<>("updated", "Updated", ItemType.INSTANCE);

        updated.valueFrom((ctx) -> {
            ItemStack i = item.getValue(ctx);
            if (unbreakable.getValue(ctx)) {
                i.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
            } else {
                i.remove(DataComponents.UNBREAKABLE);
            }
            return i;
        });
    }

    @Override
    public Node copy() {
        return new SetItemUnbreakableNode();
    }
}
