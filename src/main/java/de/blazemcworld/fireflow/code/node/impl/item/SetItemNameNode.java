package de.blazemcworld.fireflow.code.node.impl.item;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.TextType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class SetItemNameNode extends Node {

    public SetItemNameNode() {
        super("set_item_name", "Set Item Name", "Renames an item", Items.NAME_TAG);

        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);
        Input<Text> name = new Input<>("name", "Name", TextType.INSTANCE);
        Output<ItemStack> updated = new Output<>("updated", "Updated", ItemType.INSTANCE);

        updated.valueFrom((ctx) -> {
            ItemStack i = item.getValue(ctx).copy();
            i.set(DataComponentTypes.ITEM_NAME, name.getValue(ctx));
            return i;
        });
    }

    @Override
    public Node copy() {
        return new SetItemNameNode();
    }
}