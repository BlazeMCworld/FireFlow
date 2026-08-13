package de.blazemcworld.fireflow.code.node.impl.item;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.TextType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SetItemNameNode extends Node {

    public SetItemNameNode() {
        super("set_item_name", "Set Item Name", "Renames an item", Items.NAME_TAG);

        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);
        Input<Component> name = new Input<>("name", "Name", TextType.INSTANCE);
        Output<ItemStack> updated = new Output<>("updated", "Updated", ItemType.INSTANCE);

        updated.valueFrom((ctx) -> {
            ItemStack i = item.getValue(ctx).copy();
            i.set(DataComponents.ITEM_NAME, name.getValue(ctx));
            return i;
        });
    }

    @Override
    public Node copy() {
        return new SetItemNameNode();
    }
}