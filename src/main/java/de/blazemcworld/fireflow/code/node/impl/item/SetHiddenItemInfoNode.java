package de.blazemcworld.fireflow.code.node.impl.item;

import com.mojang.serialization.DataResult;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class SetHiddenItemInfoNode extends Node {

    public SetHiddenItemInfoNode() {
        super("set_hidden_item_info", "Set Hidden Item Info", "Changes which of the items components are hidden from the lore.", Items.BOOKSHELF);

        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);
        Input<ListValue<String>> hidden = new Input<>("hidden_", "Hidden", ListType.of(StringType.INSTANCE));
        Output<ItemStack> result = new Output<>("result", "Result", ItemType.INSTANCE);

        result.valueFrom(ctx -> {
            ItemStack i = item.getValue(ctx).copy();
            TooltipDisplayComponent val = TooltipDisplayComponent.DEFAULT;
            for (String s : hidden.getValue(ctx).view()) {
                DataResult<Identifier> id = Identifier.validate(s);
                if (id.isError()) continue;
                Optional<ComponentType<?>> component = Registries.DATA_COMPONENT_TYPE.getOptionalValue(id.getOrThrow());
                if (component.isEmpty()) continue;
                val = val.with(component.get(), true);
            }
            i.set(DataComponentTypes.TOOLTIP_DISPLAY, val);
            return i;
        });
    }

    @Override
    public Node copy() {
        return new SetHiddenItemInfoNode();
    }
}
