package de.blazemcworld.fireflow.code.node.impl.item;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.TextType;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class SetItemLoreNode extends Node {

    public SetItemLoreNode() {
        super("set_item_lore", "Set Item Lore", "Changes the description of an item", Items.WRITABLE_BOOK);

        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);
        Input<ListValue<Text>> lore = new Input<>("lore", "Lore", ListType.of(TextType.INSTANCE));
        Output<ItemStack> updated = new Output<>("updated", "Updated", ItemType.INSTANCE);

        updated.valueFrom((ctx) -> {
            ItemStack i = item.getValue(ctx).copy();
            i.set(DataComponentTypes.LORE, new LoreComponent(new ArrayList<>(lore.getValue(ctx).view())));
            return i;
        });
    }

    @Override
    public Node copy() {
        return new SetItemLoreNode();
    }
}