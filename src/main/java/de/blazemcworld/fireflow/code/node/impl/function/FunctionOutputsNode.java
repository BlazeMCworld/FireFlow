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
                if (ctx.functionScope.call == null) {
                    FunctionScope s = ctx.functionScope;
                    for (FunctionCallNode call : function.callNodes) {
                        ctx.submit(() -> ctx.functionScope = s);
                        ctx.sendSignal((Output<Void>) call.getOutput(name));
                        ctx.submit(() -> {
                            FunctionScope next = new FunctionScope(s, call);
                            for (Input<?> myInput : inputs) {
                                if (myInput.type == SignalType.INSTANCE) continue;
                                Object v = myInput.getValue(ctx);
                                for (Output<?> out : call.outputs) {
                                    if (!out.id.equals(myInput.id)) continue;
                                    next.scopeStore.put(out.getNode().evalUUID + "_" + out.id, v);
                                }
                            }
                            ctx.functionScope = next;
                        });
                    }
                    return;
                }
                if (ctx.functionScope.call.function != function || ctx.functionScope.parent == null) return;
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
