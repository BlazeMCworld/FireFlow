package de.blazemcworld.fireflow.code.node.impl.flow;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.SingleGenericNode;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.WireType;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.item.Items;

public class ListForEachNode<T> extends SingleGenericNode<T> {

    public ListForEachNode(WireType<T> type) {
        super("list_for_each", type == null ? "List For Each" : type.getName() + " List For Each", "For each element in the list, sends the element to the value output and the each output.", Items.HOPPER, type);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<ListValue<T>> list = new Input<>("list", "List", ListType.of(type));
        
        Output<Void> each = new Output<>("each", "Each", SignalType.INSTANCE);
        Output<T> value = new Output<>("value", "Value", type);
        Output<Double> index = new Output<>("index", "Index", NumberType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        value.valueFromScope();

        signal.onSignal((ctx) -> {
            int[] i = new int[] { 0 };
            ListValue<T> listValue = list.getValue(ctx);

            Runnable[] step = { null };
            step[0] = () -> {
                if (i[0] >= listValue.size()) {
                    ctx.sendSignal(next);
                    return;
                }
                ctx.setScopeValue(index, (double) i[0]);
                ctx.setScopeValue(value, listValue.get(i[0]++));
                ctx.submit(step[0]);
                ctx.sendSignal(each);
            };

            step[0].run();
        });
    }

    @Override
    public Node copy() {
        return new ListForEachNode<>(type);
    }

    @Override
    public Node copyWithType(WireType<?> type) {
        return new ListForEachNode<>(type);
    }
}
