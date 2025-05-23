package de.blazemcworld.fireflow.code;

import de.blazemcworld.fireflow.code.node.impl.function.FunctionCallNode;

import java.util.HashMap;

public final class FunctionScope {
    public final HashMap<String, Object> scopeStore = new HashMap<>();
    public final VariableStore varStore = new VariableStore();
    public final FunctionCallNode call;
    public final FunctionScope parent;

    public FunctionScope(FunctionScope parent, FunctionCallNode call) {
        this.parent = parent;
        this.call = call;
    }

    public FunctionScope copy() {
        FunctionScope p = parent == null ? null : parent.copy();
        FunctionScope c = new FunctionScope(p, call);
        c.scopeStore.putAll(scopeStore);
        return c;
    }
}
