package de.blazemcworld.fireflow.code.node.impl.flow;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.SignalType;
import net.minecraft.item.Items;

public class WhileNode extends Node {

    public WhileNode() {
        super("while", "While", "Repeatedly emits a signal as long as a condition is true.", Items.DAYLIGHT_DETECTOR);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<Boolean> condition = new Input<>("condition", "Condition", ConditionType.INSTANCE);
        Output<Void> repeat = new Output<>("repeat", "Repeat", SignalType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            Runnable[] step = { null };
            step[0] = () -> {
                if (condition.getValue(ctx)) {
                    ctx.submit(step[0]);
                    ctx.sendSignal(repeat);
                    return;
                }
                ctx.sendSignal(next);
            };

            step[0].run();
        });
    }

    @Override
    public Node copy() {
        return new WhileNode();
    }

}