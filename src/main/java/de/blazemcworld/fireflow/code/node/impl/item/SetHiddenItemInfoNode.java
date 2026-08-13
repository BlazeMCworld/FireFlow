package de.blazemcworld.fireflow.code.node.impl.item;

import com.mojang.serialization.DataResult;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ItemType;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.Optional;

public class SetHiddenItemInfoNode extends Node {

    public SetHiddenItemInfoNode() {
        super("set_hidden_item_info", "Set Hidden Item Info", "Changes which of the items components are hidden from the lore.", Items.BOOKSHELF);

        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);
        Input<ListValue<String>> hidden = new Input<>("hidden_", "Hidden", ListType.of(StringType.INSTANCE));
        Output<ItemStack> result = new Output<>("result", "Result", ItemType.INSTANCE);

        result.valueFrom(ctx -> {
            ItemStack i = item.getValue(ctx).copy();
            TooltipDisplay val = TooltipDisplay.DEFAULT;
            for (String s : hidden.getValue(ctx).view()) {
                DataResult<Identifier> id = Identifier.read(s);
                if (id.isError()) continue;
                Optional<DataComponentType<?>> component = BuiltInRegistries.DATA_COMPONENT_TYPE.getOptional(id.getOrThrow());
                if (component.isEmpty()) continue;
                val = val.withHidden(component.get(), true);
            }
            i.set(DataComponents.TOOLTIP_DISPLAY, val);
            return i;
        });
    }

    @Override
    public Node copy() {
        return new SetHiddenItemInfoNode();
    }
}
