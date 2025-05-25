package de.blazemcworld.fireflow.code.node.impl.event;

import de.blazemcworld.fireflow.code.CodeEvaluator;
import de.blazemcworld.fireflow.code.CodeThread;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.SignalType;
import net.minecraft.item.Items;

public class OnChunkLoadNode extends Node {

    private final Output<Void> signal;
    private final Output<Double> x;
    private final Output<Double> z;

    public OnChunkLoadNode() {
        super("on_chunk_load", "On Chunk Load", "Emits a signal when a chunk is loaded.", Items.GRASS_BLOCK);

        signal = new Output<>("signal", "Signal", SignalType.INSTANCE);
        x = new Output<>("x", "X", NumberType.INSTANCE);
        z = new Output<>("z", "Z", NumberType.INSTANCE);
        x.valueFromScope();
        z.valueFromScope();
    }

    public void emit(CodeEvaluator evaluator, double x, double z) {
        if (x < -32 || x >= 32 || z < -32 || z >= 32) return;
        CodeThread thread = evaluator.newCodeThread();
        thread.setScopeValue(this.x, x * 16);
        thread.setScopeValue(this.z, z * 16);
        thread.sendSignal(signal);
        thread.clearQueue();
    }

    @Override
    public Node copy() {
        return new OnChunkLoadNode();
    }

}
