package de.blazemcworld.fireflow.code.node.impl.item;

import com.mojang.serialization.DataResult;
import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.option.EnchantmentOptions;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.StringType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class EnchantItemNode extends Node {

    public EnchantItemNode() {
        super("enchant_item", "Enchant Item", "Adds an enchantment to an item. The enchantment will be removed if the level is less than or equal to 0, so without a level specified the enchantment will be removed.", Items.ENCHANTING_TABLE);

        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);
        Input<String> enchantment = new Input<>("enchantment", "Enchantment", StringType.INSTANCE).options(EnchantmentOptions.INSTANCE);
        Input<Double> level = new Input<>("level", "Level", NumberType.INSTANCE);
        Output<ItemStack> updated = new Output<>("updated", "Updated", ItemType.INSTANCE);

        updated.valueFrom((ctx) -> {
            ItemStack i = item.getValue(ctx).copy();
            DataResult<Identifier> id = Identifier.validate(enchantment.getValue(ctx));
            if (id.isError()) return i;
            Optional<RegistryEntry.Reference<Enchantment>> ench = FireFlow.server.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getEntry(id.getOrThrow());
            if (ench.isEmpty()) return i;
            EnchantmentHelper.apply(i, b -> b.set(ench.get(), level.getValue(ctx).intValue()));
            return i;
        });
    }

    @Override
    public Node copy() {
        return new EnchantItemNode();
    }
}
