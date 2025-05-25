package de.blazemcworld.fireflow.code.node.impl.control;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.SignalType;
import net.minecraft.item.Items;

import java.util.concurrent.atomic.AtomicInteger;

public class PauseThreadNode extends Node {
    public PauseThreadNode() {
        super("pause_thread", "Pause Thread", "Pauses the thread for a given amount of ticks.", Items.RED_BED);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<Double> ticks = new Input<>("ticks", "Ticks", NumberType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            ctx.pause();

            AtomicInteger remaining = new AtomicInteger(ticks.getValue(ctx).intValue());

            ctx.evaluator.nextTick(new Runnable() {
                @Override
                public void run() {
                    if (remaining.getAndDecrement() <= 0) {
                        ctx.sendSignal(next);
                        ctx.resume();
                    } else {
                        ctx.evaluator.nextTick(this);
                    }
                }
            });
        });
    }

    @Override
    public Node copy() {
        return new PauseThreadNode();
    }
}