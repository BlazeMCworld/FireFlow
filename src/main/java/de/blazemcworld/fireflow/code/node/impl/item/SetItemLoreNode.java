package de.blazemcworld.fireflow.code.node.impl.item;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.TextType;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;

public class SetItemLoreNode extends Node {

    public SetItemLoreNode() {
        super("set_item_lore", "Set Item Lore", "Changes the description of an item", Items.WRITABLE_BOOK);

        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);
        Input<ListValue<Component>> lore = new Input<>("lore", "Lore", ListType.of(TextType.INSTANCE));
        Output<ItemStack> updated = new Output<>("updated", "Updated", ItemType.INSTANCE);

        updated.valueFrom((ctx) -> {
            ItemStack i = item.getValue(ctx).copy();
            i.set(DataComponents.LORE, new ItemLore(new ArrayList<>(lore.getValue(ctx).view())));
            return i;
        });
    }

    @Override
    public Node copy() {
        return new SetItemLoreNode();
    }
}