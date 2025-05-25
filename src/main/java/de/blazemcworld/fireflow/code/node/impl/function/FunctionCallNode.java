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
                    FunctionScope s = ctx.functionScope;
                    ctx.submit(() -> ctx.functionScope = s);
                    ctx.sendSignal((Output<Void>) matching);
                    ctx.submit(() -> {
                        FunctionScope next = new FunctionScope(s, this);
                        for (Input<?> myInput : inputs) {
                            if (myInput.type == SignalType.INSTANCE) continue;
                            Object v = myInput.getValue(ctx);
                            for (Output<?> out : function.inputsNode.outputs) {
                                if (!out.id.equals(myInput.id)) continue;
                                next.scopeStore.put(out.getNode().evalUUID + "_" + out.id, v);
                            }
                        }
                        ctx.functionScope = next;
                    });
                });
            }
        }
        for (Input<?> matching : function.outputsNode.inputs) {
            Output<?> output = new Output<>(matching.id, matching.name, matching.type);
            if (output.type != SignalType.INSTANCE) {
                output.valueFromScope();
            }
        }
        function.callNodes.add(this);
    }

    @Override
    public Node copy() {
        return new FunctionCallNode(function);
    }

    public Output<?> getOutput(String id) {
        for (Output<?> output : outputs) {
            if (output.id.equals(id)) {
                return output;
            }
        }
        return null;
    }

}
