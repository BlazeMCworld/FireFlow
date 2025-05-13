package de.blazemcworld.fireflow.code;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.node.impl.function.FunctionCallNode;

import java.util.HashMap;

public final class FunctionScope {
    public final HashMap<Node.Output<?>, Object> store = new HashMap<>();
    public final FunctionCallNode call;
    public final FunctionScope parent;

    public FunctionScope(FunctionScope parent, FunctionCallNode call) {
        this.parent = parent;
        this.call = call;
    }

    public FunctionScope copy() {
        FunctionScope p = parent == null ? null : parent.copy();
        FunctionScope c = new FunctionScope(p, call);
        c.store.putAll(store);
        return c;
    }
}
