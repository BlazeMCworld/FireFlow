package de.blazemcworld.fireflow.code.node.impl.item;

import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.DictionaryType;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.value.DictionaryValue;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.HashMap;

public class GetItemEnchantmentsNode extends Node {

    public GetItemEnchantmentsNode() {
        super("get_item_enchantments", "Get Item Enchantments", "Returns all enchantments of an item.", Items.ENCHANTED_BOOK);

        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);
        Output<DictionaryValue<String, Double>> enchantments = new Output<>("enchantments", "Enchantments", DictionaryType.of(StringType.INSTANCE, NumberType.INSTANCE));

        enchantments.valueFrom((ctx) -> {
            ItemStack i = item.getValue(ctx);
            HashMap<String, Double> out = new HashMap<>();
            ItemEnchantments comp = i.getEnchantments();
            Registry<Enchantment> registry = FireFlow.server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            for (Holder<Enchantment> e : comp.keySet()) {
                Identifier id = registry.getKey(e.value());
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
