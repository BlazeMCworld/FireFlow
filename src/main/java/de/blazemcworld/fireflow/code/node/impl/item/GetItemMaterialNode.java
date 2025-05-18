package de.blazemcworld.fireflow.code.node.impl.item;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.StringType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

public class GetItemMaterialNode extends Node {

    public GetItemMaterialNode() {
        super("get_item_material", "Get Item Material", "Changes the type of an item", Items.BOOK);

        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);
        Output<String> material = new Output<>("material", "Material", StringType.INSTANCE);

        material.valueFrom((ctx) -> Registries.ITEM.getId(item.getValue(ctx).getItem()).getPath());
    }

    @Override
    public Node copy() {
        return new GetItemMaterialNode();
    }
}
