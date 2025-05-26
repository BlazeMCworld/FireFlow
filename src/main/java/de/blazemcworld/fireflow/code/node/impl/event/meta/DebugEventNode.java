package de.blazemcworld.fireflow.code.node.impl.event.meta;

import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.StringType;
import net.minecraft.item.Items;

public class DebugEventNode extends Node {

    private final Input<String> id;
    private final Output<Void> signal;

    public DebugEventNode() {
        super("debug_event", "Debug Event", "An event that can be triggered manually using /debug.", Items.STRUCTURE_VOID);

        id = new Input<>("id", "ID", StringType.INSTANCE);
        signal = new Output<>("signal", "Signal", SignalType.INSTANCE);
    }

    public boolean trigger(CodeEvaluator evaluator, String id) {
        CodeThread debugThread = evaluator.newCodeThread();
        debugThread.markDebug();
        if (!this.id.getValue(debugThread).equals(id)) return false;
        debugThread.sendSignal(signal);
        debugThread.clearQueue();
        return true;
    }

    @Override
    public Node copy() {
        return new DebugEventNode();
    }

}
