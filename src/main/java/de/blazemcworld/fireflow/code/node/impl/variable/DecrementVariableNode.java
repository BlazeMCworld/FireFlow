package de.blazemcworld.fireflow.code.node.impl.variable;

import de.blazemcworld.fireflow.code.VariableStore;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.StringType;
import net.minecraft.item.Items;

public class DecrementVariableNode extends Node {
    public DecrementVariableNode() {
        super("decrement_variable", "Decrement Variable", "Decreases the value of a number variable by one.", Items.WOODEN_HOE);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<String> name = new Input<>("name", "Name", StringType.INSTANCE);
        Input<String> scope = new Input<>("scope", "Scope", StringType.INSTANCE)
                .options("thread", "session", "saved");
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            VariableStore store = switch (scope.getValue(ctx)) {
                case "saved" -> ctx.evaluator.space.savedVariables;
                case "session" -> ctx.evaluator.sessionVariables;
                case "thread" -> ctx.threadVariables;
                default -> null;
            };
            if (store != null) {
                String id = name.getValue(ctx);
                store.set(id, NumberType.INSTANCE, store.get(id, NumberType.INSTANCE) - 1);
            }
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new DecrementVariableNode();
    }
}