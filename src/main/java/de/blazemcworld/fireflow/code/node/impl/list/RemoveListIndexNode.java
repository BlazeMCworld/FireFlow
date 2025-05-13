package de.blazemcworld.fireflow.code.node.impl.list;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.SingleGenericNode;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.WireType;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.item.Items;

public class RemoveListIndexNode<T> extends SingleGenericNode<T> {

    public RemoveListIndexNode(WireType<T> type) {
        super("remove_list_index", type == null ? "Remove List Index" : "Remove " + type.getName() + " List Index", "Removes an item from a list at the specified position", Items.TNT, type);

        Input<ListValue<T>> list = new Input<>("list", "List", ListType.of(type));
        Input<Double> index = new Input<>("index", "Index", NumberType.INSTANCE);

        Output<ListValue<T>> output = new Output<>("updated", "Updated", ListType.of(type));
        output.valueFrom((ctx) -> list.getValue(ctx).remove(index.getValue(ctx).intValue()));
    }

    @Override
    public Node copy() {
        return new RemoveListIndexNode<>(type);
    }

    @Override
    public Node copyWithType(WireType<?> type) {
        return new RemoveListIndexNode<>(type);
    }
}

