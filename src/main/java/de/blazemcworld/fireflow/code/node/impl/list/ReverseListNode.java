package de.blazemcworld.fireflow.code.node.impl.list;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.SingleGenericNode;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.WireType;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.item.Items;

public class ReverseListNode<T> extends SingleGenericNode<T> {

    public ReverseListNode(WireType<T> type) {
        super("reverse_list", type == null ? "Reverse List" : "Reverse " + type.getName() + " List", "Reverses the order of elements in a list.", Items.ENDER_CHEST, type);

        Input<ListValue<T>> list = new Input<>("list", "List", ListType.of(type));
        Output<ListValue<T>> reversed = new Output<>("reversed", "Reversed", ListType.of(type));

        reversed.valueFrom((ctx) -> list.getValue(ctx).reversed());
    }

    @Override
    public Node copy() {
        return new ReverseListNode<>(type);
    }

    @Override
    public Node copyWithType(WireType<?> type) {
        return new ReverseListNode<>(type);
    }
}
