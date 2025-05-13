package de.blazemcworld.fireflow.code.node.impl.list;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.SingleGenericNode;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.WireType;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.item.Items;

public class CreateListNode<T> extends SingleGenericNode<T> {

    public CreateListNode(WireType<T> type) {
        super("create_list", type == null ? "Create List" : "Create " + type.getName() + " List", "Creates a new list with the given contents.", Items.MINECART, type);

        Varargs<T> content = new Varargs<>("content", "Content", type);
        Output<ListValue<T>> output = new Output<>("list", "List", ListType.of(type));
        output.valueFrom((ctx) -> {
            return new ListValue<>(type, content.getVarargs(ctx));
        });
    }

    @Override
    public Node copy() {
        return new CreateListNode<>(type);
    }

    @Override
    public Node copyWithType(WireType<?> type) {
        return new CreateListNode<>(type);
    }
}
