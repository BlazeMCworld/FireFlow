package de.blazemcworld.fireflow.code.node.impl.item;

import com.mojang.serialization.DataResult;
import de.blazemcworld.fireflow.FireFlow;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.option.EnchantmentOptions;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.StringType;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

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
            DataResult<Identifier> id = Identifier.read(enchantment.getValue(ctx));
            if (id.isError()) return i;
            Optional<Holder.Reference<Enchantment>> ench = FireFlow.server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(id.getOrThrow());
            if (ench.isEmpty()) return i;
            EnchantmentHelper.updateEnchantments(i, b -> b.set(ench.get(), level.getValue(ctx).intValue()));
            return i;
        });
    }

    @Override
    public Node copy() {
        return new EnchantItemNode();
    }
}
