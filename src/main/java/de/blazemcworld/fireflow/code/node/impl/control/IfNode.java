package de.blazemcworld.fireflow.code.node.impl.control;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.SignalType;
import net.minecraft.item.Items;

public class IfNode extends Node {

    public IfNode() {
        super("if", "If", "Depending on the condition, sends the matching output.", Items.COMPARATOR);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<Boolean> condition = new Input<>("condition", "Condition", ConditionType.INSTANCE);

        Output<Void> trueOut = new Output<>("true", "True", SignalType.INSTANCE);
        Output<Void> falseOut = new Output<>("false", "False", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            if (condition.getValue(ctx)) {
                ctx.sendSignal(trueOut);
            } else {
                ctx.sendSignal(falseOut);
            }
        });
    }

    @Override
    public Node copy() {
        return new IfNode();
    }
}

