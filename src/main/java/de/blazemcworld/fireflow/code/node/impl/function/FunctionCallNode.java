package de.blazemcworld.fireflow.code.node.impl.function;

import de.blazemcworld.fireflow.code.CodeEvaluator;
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
                    ctx.submit(() -> ctx.functionScope = prev);
                    ctx.sendSignal((Output<Void>) matching);
                    ctx.submit(() -> ctx.functionScope = ctx.functionScope.child(this));
                });
            }
        }
        for (Input<?> matching : function.outputsNode.inputs) {
            Output<?> output = new Output<>(matching.id, matching.name, matching.type);
            if (output.type != SignalType.INSTANCE) {
                ((Output<Object>) output).valueFrom((ctx) -> {
                    FunctionScope prev = ctx.functionScope;
                    ctx.functionScope = ctx.functionScope.child(this);
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

    public Input<?> getInput(String name, CodeEvaluator evaluator) {
        evaluator.syncRevision(this);
        for (Input<?> input : inputs) {
            if (input.id.equals(name)) {
                return input;
            }
        }
        return null;
    }

    public Output<?> getOutput(String name, CodeEvaluator evaluator) {
        evaluator.syncRevision(this);
        for (Output<?> output : outputs) {
            if (output.id.equals(name)) {
                return output;
            }
        }
        return null;
    }

}
