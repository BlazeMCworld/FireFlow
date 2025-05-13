package de.blazemcworld.fireflow.code.node.impl.list;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.SingleGenericNode;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.WireType;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.item.Items;

public class ListValuesEqualsNode<T> extends SingleGenericNode<T> {

    public ListValuesEqualsNode(WireType<T> type) {
        super("list_values_equal", 
              type == null ? "List Values Equal" : type.getName() + " List Values Equal", 
              "Checks if the current value at the specified index in the list equals the given value.", 
              Items.COMPARATOR, 
              type);

        Input<ListValue<T>> list = new Input<>("list", "List", ListType.of(type));
        Input<Double> index = new Input<>("index", "Index", NumberType.INSTANCE);
        Input<T> value = new Input<>("value", "Value", type);

        Output<Boolean> output = new Output<>("equals", "Equals", ConditionType.INSTANCE);
        output.valueFrom((ctx) -> {
            ListValue<T> listValue = list.getValue(ctx);
            int idx = index.getValue(ctx).intValue();
            T val = value.getValue(ctx);
            
            if (idx < 0 || idx >= listValue.size()) {
                return false;
            }
            
            T listItem = listValue.get(idx);
            return type.valuesEqual(listItem, val);
        });
    }

    @Override
    public Node copy() {
        return new ListValuesEqualsNode<>(type);
    }

    @Override
    public Node copyWithType(WireType<?> type) {
        return new ListValuesEqualsNode<>(type);
    }
}
