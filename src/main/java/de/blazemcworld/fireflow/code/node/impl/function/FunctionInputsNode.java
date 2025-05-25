package de.blazemcworld.fireflow.code.node.impl.function;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.WireType;

public class FunctionInputsNode extends Node {
    
    public final FunctionDefinition function;

    public FunctionInputsNode(FunctionDefinition function) {
        super("function_inputs", function.name + " Inputs", "", function.icon);
        this.function = function;
    }

    public void addInput(String name, WireType<?> type) {
        Output<?> input = new Output<>(name, name, type);
        if (type != SignalType.INSTANCE) {
            input.valueFromScope();
        }
    }

    @Override
    public Node copy() {
        return new FunctionInputsNode(function);
    }

}
