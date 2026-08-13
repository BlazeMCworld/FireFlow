package de.blazemcworld.fireflow.code.node.impl.item;

import com.mojang.serialization.DataResult;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.StringType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;

public class SetItemMaterialNode extends Node {

    public SetItemMaterialNode() {
        super("set_item_material", "Set Item Material", "Changes the type of an item", Items.PAPER);

        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);
        Input<String> material = new Input<>("material", "Material", StringType.INSTANCE);
        Output<ItemStack> updated = new Output<>("updated", "Updated", ItemType.INSTANCE);

        updated.valueFrom((ctx) -> {
            DataResult<Identifier> id = Identifier.read(material.getValue(ctx));
            Optional<Item> mat = id.isSuccess() ? BuiltInRegistries.ITEM.getOptional(id.getOrThrow()) : Optional.empty();
            ItemStack i = item.getValue(ctx);
            return mat.map(i::transmuteCopy).orElse(i);
        });
    }

    @Override
    public Node copy() {
        return new SetItemMaterialNode();
    }
}
