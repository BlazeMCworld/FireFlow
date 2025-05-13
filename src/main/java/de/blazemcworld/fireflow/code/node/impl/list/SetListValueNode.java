package de.blazemcworld.fireflow.code.node.impl.list;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.SingleGenericNode;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.WireType;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.item.Items;

public class SetListValueNode<T> extends SingleGenericNode<T> {

    public SetListValueNode(WireType<T> type) {
        super("set_list_value", type == null ? "Set List Value" : "Set " + type.getName() + " List Value", "Sets a value at a specific index in a list", Items.BUNDLE, type);

        Input<ListValue<T>> list = new Input<>("list", "List", ListType.of(type));
        Input<Double> index = new Input<>("index", "Index", NumberType.INSTANCE);
        Input<T> value = new Input<>("value", "Value", type);

        Output<ListValue<T>> updated = new Output<>("updated", "Updated", ListType.of(type));

        updated.valueFrom((ctx) -> {
            ListValue<T> listValue = list.getValue(ctx);
            return listValue.set(index.getValue(ctx).intValue(), value.getValue(ctx));
        });
    }

    @Override
    public Node copy() {
        return new SetListValueNode<>(type);
    }

    @Override
    public Node copyWithType(WireType<?> type) {
        return new SetListValueNode<>(type);
    }
}
