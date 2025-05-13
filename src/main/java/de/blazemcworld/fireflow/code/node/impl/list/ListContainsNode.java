package de.blazemcworld.fireflow.code.node.impl.list;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.SingleGenericNode;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.WireType;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.item.Items;

import java.util.HashSet;

public class ListContainsNode<T> extends SingleGenericNode<T> {

    public ListContainsNode(WireType<T> type) {
        super("list_contains", "List Contains", "Checks if a list contains all specified values", Items.ARROW, type);

        Input<ListValue<T>> list = new Input<>("list", "List", ListType.of(type));
        Varargs<T> check = new Varargs<>("value", "Value", type);
        Output<Boolean> contains = new Output<>("result", "Result", ConditionType.INSTANCE);

        contains.valueFrom((ctx) -> {
            ListValue<T> v = list.getValue(ctx);
            HashSet<T> missing = new HashSet<>(check.getVarargs(ctx));
            for (int i = 0; i < v.size(); i++) {
                T val = v.get(i);
                missing.removeIf(x -> type.valuesEqual(val, x));
            }
            return missing.isEmpty();
        });
    }

    @Override
    public Node copy() {
        return new ListContainsNode<>(type);
    }

    @Override
    public Node copyWithType(WireType<?> type) {
        return new ListContainsNode<>(type);
    }
}

