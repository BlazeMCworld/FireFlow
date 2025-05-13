package de.blazemcworld.fireflow.code.node.impl.function;

import de.blazemcworld.fireflow.code.FunctionScope;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.SignalType;

public class FunctionCallNode extends Node {

    public final FunctionDefinition function;

    @SuppressWarnings("unchecked")
    public FunctionCallNode(FunctionDefinition function) {
        super("function_call", function.name, "", function.icon);
        this.function = function;

        for (Output<?> matching : function.inputsNode.outputs) {
            Input<?> input = new Input<>(matching.id, matching.name, matching.type);
            if (input.type == SignalType.INSTANCE) {
                input.onSignal((ctx) -> {

                    FunctionScope prev = ctx.functionScope;
                    ctx.functionScope = new FunctionScope(prev, this);
                    ctx.submit(() -> ctx.functionScope = prev);
                    ctx.sendSignal((Output<Void>) matching);
                });
            }
        }
        for (Input<?> matching : function.outputsNode.inputs) {
            Output<?> output = new Output<>(matching.id, matching.name, matching.type);
            if (output.type != SignalType.INSTANCE) {
                ((Output<Object>) output).valueFrom((ctx) -> {
                    FunctionScope prev = ctx.functionScope;
                    ctx.functionScope = new FunctionScope(prev, this);
                    Object out = matching.getValue(ctx);
                    ctx.functionScope = prev;
                    return out;
                });
            }
        }
        function.callNodes.add(this);
    }

    @Override
    public Node copy() {
        return new FunctionCallNode(function);
    }

    public Input<?> getInput(String name) {
        for (Input<?> input : inputs) {
            if (input.id.equals(name)) {
                return input;
            }
        }
        return null;
    }

    public Output<?> getOutput(String name) {
        for (Output<?> output : outputs) {
            if (output.id.equals(name)) {
                return output;
            }
        }
        return null;
    }

}
