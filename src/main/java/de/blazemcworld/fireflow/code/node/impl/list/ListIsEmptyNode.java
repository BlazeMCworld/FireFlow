package de.blazemcworld.fireflow.code.node.impl.list;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.SingleGenericNode;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.WireType;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.item.Items;

public class ListIsEmptyNode<T> extends SingleGenericNode<T> {

    public ListIsEmptyNode(WireType<T> type) {
        super("list_is_empty", type == null ? "List Is Empty" : type.getName() + " List Is Empty", "Checks if a list is empty.", Items.BARRIER, type);

        Input<ListValue<T>> list = new Input<>("list", "List", ListType.of(type));
        Output<Boolean> isEmpty = new Output<>("is_empty", "Is Empty", ConditionType.INSTANCE);

        isEmpty.valueFrom((ctx) -> list.getValue(ctx).size() == 0);
    }

    @Override
    public Node copy() {
        return new ListIsEmptyNode<>(type);
    }

    @Override
    public Node copyWithType(WireType<?> type) {
        return new ListIsEmptyNode<>(type);
    }
}
