package de.blazemcworld.fireflow.code.node.impl.list;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.SingleGenericNode;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.WireType;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.item.Items;

public class TrimListNode<T> extends SingleGenericNode<T> {

    public TrimListNode(WireType<T> type) {
        super("trim_list", type == null ? "Trim List" : "Trim " + type.getName() + " List", "Removes all null values from a list.", Items.SHEARS, type);

        Input<ListValue<T>> list = new Input<>("list", "List", ListType.of(type));
        Input<Double> start = new Input<>("start", "Start", NumberType.INSTANCE);
        Input<Double> end = new Input<>("end", "End", NumberType.INSTANCE);
        Output<ListValue<T>> trimmed = new Output<>("trimmed", "Trimmed", ListType.of(type));

        trimmed.valueFrom((ctx) -> list.getValue(ctx).trim(start.getValue(ctx).intValue(), end.getValue(ctx).intValue()));
    }

    @Override
    public Node copy() {
        return new TrimListNode<>(type);
    }

    @Override
    public Node copyWithType(WireType<?> type) {
        return new TrimListNode<>(type);
    }

}
