package de.blazemcworld.fireflow.code;

import de.blazemcworld.fireflow.code.node.impl.function.FunctionCallNode;

import java.util.HashMap;

public final class FunctionScope {
    public final HashMap<String, Object> scopeStore = new HashMap<>();
    public final VariableStore varStore = new VariableStore();
    public final FunctionCallNode call;
    public final FunctionScope parent;
    private final HashMap<String, FunctionScope> children = new HashMap<>();

    public FunctionScope(FunctionScope parent, FunctionCallNode call) {
        this.parent = parent;
        this.call = call;
    }

    public FunctionScope simpleCopy() {
        FunctionScope c = new FunctionScope(null, call);
        c.scopeStore.putAll(scopeStore);
        return c;
    }

    public FunctionScope child(FunctionCallNode call) {
        return children.computeIfAbsent(call.evalUUID, c -> new FunctionScope(this, call));
    }

    public FunctionScope fakeChild(FunctionCallNode call, FunctionScope value) {
        children.put(call.evalUUID, value);
        return value;
    }
}
