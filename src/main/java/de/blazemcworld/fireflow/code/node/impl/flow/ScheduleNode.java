package de.blazemcworld.fireflow.code.node.impl.flow;

import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.SignalType;
import net.minecraft.item.Items;

import java.util.concurrent.atomic.AtomicInteger;

public class ScheduleNode extends Node {

    public ScheduleNode() {
        super("schedule", "Schedule", "Schedules a signal to be emitted after a given amount of ticks.", Items.CLOCK);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<Double> delay = new Input<>("delay", "Delay", NumberType.INSTANCE);

        Output<Void> now = new Output<>("now", "Now", SignalType.INSTANCE);
        Output<Void> task = new Output<>("task", "Task", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            AtomicInteger remaining = new AtomicInteger(delay.getValue(ctx).intValue());

            CodeThread spawned = ctx.subThread();

            ctx.evaluator.tickTasks.add(new Runnable() {
                @Override
                public void run() {
                    if (remaining.getAndDecrement() <= 0) {
                        ctx.evaluator.tickTasks.remove(this);
                        spawned.sendSignal(task);
                        spawned.clearQueue();
                    }
                }
            });

            ctx.sendSignal(now);
        });
    }

    @Override
    public Node copy() {
        return new ScheduleNode();
    }
}
