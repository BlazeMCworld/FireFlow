package de.blazemcworld.fireflow.code.node.impl.item;

import com.mojang.serialization.DataResult;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.StringType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class SetItemMaterialNode extends Node {

    public SetItemMaterialNode() {
        super("set_item_material", "Set Item Material", "Changes the type of an item", Items.PAPER);

        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);
        Input<String> material = new Input<>("material", "Material", StringType.INSTANCE);
        Output<ItemStack> updated = new Output<>("updated", "Updated", ItemType.INSTANCE);

        updated.valueFrom((ctx) -> {
            DataResult<Identifier> id = Identifier.validate(material.getValue(ctx));
            Optional<Item> mat = id.isSuccess() ? Registries.ITEM.getOptionalValue(id.getOrThrow()) : Optional.empty();
            ItemStack i = item.getValue(ctx);
            if (mat.isPresent()) return i.withItem(mat.get());
            return i;
        });
    }

    @Override
    public Node copy() {
        return new SetItemMaterialNode();
    }
}
