package de.blazemcworld.fireflow.code.node.impl.variable;

import de.blazemcworld.fireflow.code.VariableStore;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.SingleGenericNode;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.type.WireType;
import net.minecraft.item.Items;

public class SetVariableNode<T> extends SingleGenericNode<T> {

    public SetVariableNode(WireType<T> type) {
        super(
                "set_variable", type == null ? "Set Variable" : "Set " + type.getName() + " Variable",
                "Changes the value of a variable.", Items.IRON_BLOCK, type
        );

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<String> name = new Input<>("name", "Name", StringType.INSTANCE);
        Input<String> scope = new Input<>("scope", "Scope", StringType.INSTANCE)
                .options("thread", "session", "saved");
        Input<T> value = new Input<>("value", "Value", type);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            VariableStore store = switch (scope.getValue(ctx)) {
                case "saved" -> ctx.evaluator.space.savedVariables;
                case "session" -> ctx.evaluator.sessionVariables;
                case "thread" -> ctx.threadVariables;
                default -> null;
            };
            if (store != null) store.set(name.getValue(ctx), type, value.getValue(ctx));
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetVariableNode<>(type);
    }

    @Override
    public Node copyWithType(WireType<?> type) {
        return new SetVariableNode<>(type);
    }
}
