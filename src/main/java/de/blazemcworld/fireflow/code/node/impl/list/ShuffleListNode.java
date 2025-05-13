package de.blazemcworld.fireflow.code.node.impl.list;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.SingleGenericNode;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.WireType;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShuffleListNode<T> extends SingleGenericNode<T> {

    public ShuffleListNode(WireType<T> type) {
        super(
            "shuffle_list",
            type == null ? "Shuffle List" : "Shuffle " + type.getName() + " List",
            "Randomly shuffles the order of elements in the list.",
            Items.SOUL_CAMPFIRE,
            type
        );

        Input<ListValue<T>> list = new Input<>("list", "List", ListType.of(type));
        
        Output<ListValue<T>> output = new Output<>("shuffled", "Shuffled", ListType.of(type));
        output.valueFrom((ctx) -> {
            ListValue<T> listValue = list.getValue(ctx);
            List<T> shuffled = new ArrayList<>(listValue.view());
            Collections.shuffle(shuffled);
            return new ListValue<>(type, shuffled);
        });
    }

    @Override
    public Node copy() {
        return new ShuffleListNode<>(type);
    }

    @Override
    public Node copyWithType(WireType<?> type) {
        return new ShuffleListNode<>(type);
    }
}
