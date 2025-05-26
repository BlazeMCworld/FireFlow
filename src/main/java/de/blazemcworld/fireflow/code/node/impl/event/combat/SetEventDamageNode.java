package de.blazemcworld.fireflow.code.node.impl.event.combat;

import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.SignalType;
import net.minecraft.item.Items;

public class SetEventDamageNode extends Node {

    public SetEventDamageNode() {
        super("set_event_damage", "Set Event Damage", "Changes the amount of damage done by an event. Only works on attack & hurt events.", Items.RED_DYE);

        Input<Void> signal = new Input<>("signal", "Signal", SignalType.INSTANCE);
        Input<Double> amount = new Input<>("amount", "Amount", NumberType.INSTANCE);
        Output<Void> next = new Output<>("next", "Next", SignalType.INSTANCE);

        signal.onSignal((ctx) -> {
            if (ctx.context.type == CodeThread.EventType.DAMAGE_EVENT) {
                ctx.context.eventNumber = amount.getValue(ctx);
            }
            ctx.sendSignal(next);
        });
    }

    @Override
    public Node copy() {
        return new SetEventDamageNode();
    }
}
