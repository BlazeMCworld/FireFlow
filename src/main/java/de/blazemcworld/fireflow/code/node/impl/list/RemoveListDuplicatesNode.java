package de.blazemcworld.fireflow.code.node.impl.list;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.SingleGenericNode;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.WireType;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

public class RemoveListDuplicatesNode<T> extends SingleGenericNode<T> {

    public RemoveListDuplicatesNode(WireType<T> type) {
        super(
            "remove_list_duplicates",
            type == null ? "Remove List Duplicates" : "Remove " + type.getName() + " List Duplicates",
            "Removes duplicate values from the list while preserving the order of first occurrence and returns a list containing only unique values.",
            Items.HOPPER,
            type
        );

        Input<ListValue<T>> list = new Input<>("list", "List", ListType.of(type));
        
        Output<ListValue<T>> output = new Output<>("unique_list", "Unique", ListType.of(type));
        output.valueFrom((ctx) -> {
            ListValue<T> listValue = list.getValue(ctx);
            List<T> result = new ArrayList<>();
            
            main:
            for (T item : listValue.view()) {
                for (T resultItem : result) {
                    if (type.valuesEqual(resultItem, item)) {
                        continue main;
                    }
                }
                result.add(item);
            }
            
            return new ListValue<>(type, result);
        });
    }

    @Override
    public Node copy() {
        return new RemoveListDuplicatesNode<>(type);
    }

    @Override
    public Node copyWithType(WireType<?> type) {
        return new RemoveListDuplicatesNode<>(type);
    }
}
