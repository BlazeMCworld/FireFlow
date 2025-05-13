package de.blazemcworld.fireflow.code.node.impl.event;

import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.SignalType;
import net.minecraft.item.Items;

public class OnInitializeNode extends Node {

    private final Output<Void> signal;

    public OnInitializeNode() {
        super("on_initialize", "On Initialize", "Emits a signal when the space loads or reloads.", Items.CHERRY_SAPLING);

        signal = new Output<>("signal", "Signal", SignalType.INSTANCE);
    }

    @Override
    public Node copy() {
        return new OnInitializeNode();
    }

    public void emit(CodeEvaluator evaluator) {
        CodeThread thread = evaluator.newCodeThread();
        thread.sendSignal(signal);
        thread.clearQueue();
    }

}
