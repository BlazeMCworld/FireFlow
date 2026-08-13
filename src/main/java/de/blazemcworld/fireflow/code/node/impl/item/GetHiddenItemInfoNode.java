package de.blazemcworld.fireflow.code.node.impl.item;

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

import java.util.ArrayList;

public class GetHiddenItemInfoNode extends Node {

    public GetHiddenItemInfoNode() {
        super("get_hidden_item_info", "Get Hidden Item Info", "Returns which of the items components are hidden from the lore.", Items.CHISELED_BOOKSHELF);

        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);
        Output<ListValue<String>> hidden = new Output<>("hidden_info", "Hidden", ListType.of(StringType.INSTANCE));

        hidden.valueFrom(ctx -> {
            TooltipDisplay comp = item.getValue(ctx).getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
            ArrayList<String> list = new ArrayList<>();
            for (DataComponentType<?> type : comp.hiddenComponents()) {
                Identifier id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
                if (id == null) continue;
                list.add(id.getPath());
            }
            return new ListValue<>(StringType.INSTANCE, list);
        });
    }

    @Override
    public Node copy() {
        return new GetHiddenItemInfoNode();
    }
}
