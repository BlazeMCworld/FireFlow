package de.blazemcworld.fireflow.code.node.impl.variable;

import de.blazemcworld.fireflow.code.VariableStore;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.SingleGenericNode;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.type.WireType;
import net.minecraft.item.Items;

public class GetVariableNode<T> extends SingleGenericNode<T> {

    public GetVariableNode(WireType<T> type) {
        super("get_variable", type == null ? "Get Variable" : "Get " + type.getName() + " Variable", "Returns the value of a variable.", Items.IRON_INGOT, type);

        Input<String> name = new Input<>("name", "Name", StringType.INSTANCE);
        Input<String> scope = new Input<>("scope", "Scope", StringType.INSTANCE)
                .options("Thread", "Session", "Saved");
        Output<T> value = new Output<>("value", "Value", type);

        value.valueFrom((ctx) -> {
            VariableStore store = switch (scope.getValue(ctx)) {
                case "Saved" -> ctx.evaluator.space.savedVariables;
                case "Session" -> ctx.evaluator.sessionVariables;
                case "Thread" -> ctx.threadVariables;
                default -> null;
            };
            if (store == null) return type.defaultValue();
            return store.get(name.getValue(ctx), type);
        });
    }

    @Override
    public Node copy() {
        return new GetVariableNode<>(type);
    }

    @Override
    public Node copyWithType(WireType<?> type) {
        return new GetVariableNode<>(type);
    }
}
