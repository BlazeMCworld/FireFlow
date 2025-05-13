package de.blazemcworld.fireflow.code.node.impl.function;

import de.blazemcworld.fireflow.code.FunctionScope;
import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.SignalType;
import de.blazemcworld.fireflow.code.type.WireType;

public class FunctionOutputsNode extends Node {
    
    public FunctionDefinition function;

    public FunctionOutputsNode(FunctionDefinition function) {
        super("function_outputs", function.name + " Outputs", "", function.icon);
        this.function = function;
    }

    @SuppressWarnings("unchecked")
    public void addOutput(String name, WireType<?> type) {
        Input<?> output = new Input<>(name, name, type);
        if (type == SignalType.INSTANCE) {
            output.onSignal((ctx) -> {
                if (ctx.functionScope == null) {
                    for (FunctionCallNode call : function.callNodes) {
                        ctx.sendSignal((Output<Void>) call.getOutput(name));
                    }
                    return;
                }
                if (ctx.functionScope.call.function != function) return;
                FunctionScope s = ctx.functionScope;
                ctx.functionScope = ctx.functionScope.parent;
                ctx.submit(() -> ctx.functionScope = s);
                ctx.sendSignal((Output<Void>) s.call.getOutput(name));
            });
        }
    }

    @Override
    public Node copy() {
        return new FunctionOutputsNode(function);
    }

}
