package de.blazemcworld.fireflow.code.node.impl.item;

import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.DictionaryType;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.value.DictionaryValue;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public class GetItemEnchantmentsNode extends Node {

    public GetItemEnchantmentsNode() {
        super("get_item_enchantments", "Get Item Enchantments", "Returns all enchantments of an item.", Items.ENCHANTED_BOOK);

        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);
        Output<DictionaryValue<String, Double>> enchantments = new Output<>("enchantments", "Enchantments", DictionaryType.of(StringType.INSTANCE, NumberType.INSTANCE));

        enchantments.valueFrom((ctx) -> {
            ItemStack i = item.getValue(ctx);
            HashMap<String, Double> out = new HashMap<>();
            ItemEnchantmentsComponent comp = i.getEnchantments();
            Registry<Enchantment> registry = FireFlow.server.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
            for (RegistryEntry<Enchantment> e : comp.getEnchantments()) {
                Identifier id = registry.getId(e.value());
                if (id == null) continue;
                out.put(id.getPath(), (double) comp.getLevel(e));
            }

            return new DictionaryValue<>(StringType.INSTANCE, NumberType.INSTANCE, out);
        });
    }

    @Override
    public Node copy() {
        return new GetItemEnchantmentsNode();
    }

}
