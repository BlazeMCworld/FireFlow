package de.blazemcworld.fireflow.code.node.impl.list;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.SingleGenericNode;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.WireType;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.item.Items;

public class GetListValueNode<T> extends SingleGenericNode<T> {

    public GetListValueNode(WireType<T> type) {
        super("get_list_value", type == null ? "Get List Value" : "Get " + type.getName() + " List Value", "Returns the value at the given index in the list.", Items.HOPPER, type);

        Input<ListValue<T>> list = new Input<>("list", "List", ListType.of(type));
        Input<Double> index = new Input<>("index", "Index", NumberType.INSTANCE);

        Output<T> output = new Output<>("value", "Value", type);
        output.valueFrom((ctx) -> list.getValue(ctx).get(index.getValue(ctx).intValue()));
    }

    @Override
    public Node copy() {
        return new GetListValueNode<>(type);
    }

    @Override
    public Node copyWithType(WireType<?> type) {
        return new GetListValueNode<>(type);
    }
}

