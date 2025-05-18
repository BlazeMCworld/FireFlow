package de.blazemcworld.fireflow.code.node.impl.item;

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

import java.util.ArrayList;

public class GetHiddenItemInfoNode extends Node {

    public GetHiddenItemInfoNode() {
        super("get_hidden_item_info", "Get Hidden Item Info", "Returns which of the items components are hidden from the lore.", Items.CHISELED_BOOKSHELF);

        Input<ItemStack> item = new Input<>("item", "Item", ItemType.INSTANCE);
        Output<ListValue<String>> hidden = new Output<>("hidden_info", "Hidden", ListType.of(StringType.INSTANCE));

        hidden.valueFrom(ctx -> {
            TooltipDisplayComponent comp = item.getValue(ctx).getOrDefault(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplayComponent.DEFAULT);
            ArrayList<String> list = new ArrayList<>();
            for (ComponentType<?> type : comp.hiddenComponents()) {
                Identifier id = Registries.DATA_COMPONENT_TYPE.getId(type);
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
