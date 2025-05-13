package de.blazemcworld.fireflow.code.node.impl.list;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.SingleGenericNode;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.WireType;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.item.Items;

public class IndexInListNode<T> extends SingleGenericNode<T> {

    public IndexInListNode(WireType<T> type) {
        super(
            "index_in_list", 
             type == null ? "Index In List" : "Index In " + type.getName() + " List", 
             "Finds the index of a value in a list. Returns -1 if not found.", 
             Items.COMPASS, 
             type
        );

        Input<ListValue<T>> list = new Input<>("list", "List", ListType.of(type));
        Input<T> value = new Input<>("value", "Value", type);

        Output<Double> index = new Output<>("index", "Index", NumberType.INSTANCE);

        index.valueFrom((ctx) -> {
            ListValue<T> listValue = list.getValue(ctx);
            T search = value.getValue(ctx);
            for (int i = 0; i < listValue.size(); i++) {
                if (type.valuesEqual(listValue.get(i), search)) return (double) i;
            }
            return -1.0;
        });
    }

    @Override
    public Node copy() {
        return new IndexInListNode<>(type);
    }

    @Override
    public Node copyWithType(WireType<?> type) {
        return new IndexInListNode<>(type);
    }
}
