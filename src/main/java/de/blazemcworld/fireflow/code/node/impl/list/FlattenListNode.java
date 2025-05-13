package de.blazemcworld.fireflow.code.node.impl.list;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.SingleGenericNode;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.WireType;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

public class FlattenListNode<T> extends SingleGenericNode<T> {

    public FlattenListNode(WireType<T> type) {
        super(
            "flatten_list",
            type == null ? "Flatten List" : "Flatten " + type.getName() + " List",
            "Flattens a list of lists into a single list.",
            Items.PURPUR_SLAB,
            type
        );

        Input<ListValue<ListValue<T>>> nested = new Input<>("nested", "Nested", ListType.of(ListType.of(type)));
        
        Output<ListValue<T>> flattened = new Output<>("flattened", "Flattened", ListType.of(type));
        flattened.valueFrom((ctx) -> {
            ListValue<ListValue<T>> inputList = nested.getValue(ctx);
            List<T> result = new ArrayList<>();
            
            for (ListValue<T> sublist : inputList.view()) {
                result.addAll(sublist.view());
            }
            
            return new ListValue<>(type, result);
        });
    }

    @Override
    public Node copy() {
        return new FlattenListNode<>(type);
    }

    @Override
    public Node copyWithType(WireType<?> type) {
        return new FlattenListNode<>(type);
    }
}
