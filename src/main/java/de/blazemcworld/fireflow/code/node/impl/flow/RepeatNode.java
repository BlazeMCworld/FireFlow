package de.blazemcworld.fireflow.code.node.impl.flow;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.SignalType;
import net.minecraft.item.Items;

public class RepeatNode extends Node {
    
    public RepeatNode() {
        super("repeat", "Repeat", "Emits a signal a given amount of times.", Items.REPEATER);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<Double> times = new Input<>("times", "Times", NumberType.INSTANCE);
        Output<Void> repeat = new Output<>("repeat", "Repeat", SignalType.INSTANCE);
        Output<Double> index = new Output<>("index", "Index", NumberType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);
        index.valueFromScope();

        signal.onSignal((ctx) -> {
            int max = times.getValue(ctx).intValue();
            double[] i = new double[] { 0 };

            Runnable[] step = { null };
            step[0] = () -> {
                if (i[0] >= max) {
                    ctx.sendSignal(next);
                    return;
                }
                ctx.setScopeValue(index, i[0]++);
                ctx.submit(step[0]);
                ctx.sendSignal(repeat);
            };

            step[0].run();
        });
    }

    @Override
    public Node copy() {
        return new RepeatNode();
    }

}
