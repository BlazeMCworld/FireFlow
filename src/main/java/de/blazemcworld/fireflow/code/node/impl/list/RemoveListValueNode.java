package de.blazemcworld.fireflow.code.node.impl.list;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.SingleGenericNode;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.WireType;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

public class RemoveListValueNode<T> extends SingleGenericNode<T> {

    public RemoveListValueNode(WireType<T> type) {
        super("remove_list_value", type == null ? "Remove List Value" : "Remove " + type.getName() + " List Value", "Removes all occurrences of a value from a list", Items.TNT_MINECART, type);

        Input<ListValue<T>> list = new Input<>("list", "List", ListType.of(type));
        Input<T> value = new Input<>("value", "Value", type);

        Output<ListValue<T>> output = new Output<>("list", "List", ListType.of(type));
        output.valueFrom((ctx) -> {
            ListValue<T> listValue = list.getValue(ctx);
            T search = value.getValue(ctx);
            List<T> filtered = new ArrayList<>(listValue.size());
            for (int i = 0; i < listValue.size(); i++) {
                if (!type.valuesEqual(listValue.get(i), search)) {
                    filtered.add(listValue.get(i));
                }
            }
            return new ListValue<>(type, filtered);
        });
    }

    @Override
    public Node copy() {
        return new RemoveListValueNode<>(type);
    }

    @Override
    public Node copyWithType(WireType<?> type) {
        return new RemoveListValueNode<>(type);
    }
}

