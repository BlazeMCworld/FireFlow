package de.blazemcworld.fireflow.code.node.impl.list;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.SingleGenericNode;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.WireType;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.item.Items;

public class ListInsertNode<T> extends SingleGenericNode<T> {

    public ListInsertNode(WireType<T> type) {
        super("list_insert", type == null ? "List Insert" : type.getName() + " List Insert", "Inserts one or more values at a specific position in a list", Items.PISTON, type);

        Input<ListValue<T>> list = new Input<>("list", "List", ListType.of(type));
        Input<Double> index = new Input<>("index", "Index", NumberType.INSTANCE);
        Varargs<T> value = new Varargs<>("value", "Value", type);

        Output<ListValue<T>> output = new Output<>("result", "Result", ListType.of(type));
        output.valueFrom((ctx) -> {
            ListValue<T> listValue = list.getValue(ctx);
            return listValue.insert(index.getValue(ctx).intValue(), value.getVarargs(ctx));
        });
    }

    @Override
    public Node copy() {
        return new ListInsertNode<>(type);
    }

    @Override
    public Node copyWithType(WireType<?> type) {
        return new ListInsertNode<>(type);
    }
}