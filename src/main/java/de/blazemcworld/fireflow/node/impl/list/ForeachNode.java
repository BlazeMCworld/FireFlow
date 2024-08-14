package de.blazemcworld.fireflow.node.impl.list;

import de.blazemcworld.fireflow.compiler.CompiledNode;
import de.blazemcworld.fireflow.compiler.StructDefinition;
import de.blazemcworld.fireflow.node.Node;
import de.blazemcworld.fireflow.node.annotation.*;
import de.blazemcworld.fireflow.value.AllValues;
import de.blazemcworld.fireflow.value.ListValue;
import de.blazemcworld.fireflow.value.SignalValue;
import de.blazemcworld.fireflow.value.Value;

import java.util.ArrayList;
import java.util.List;

public class ForeachNode extends Node {

    private final Value type;

    public ForeachNode(Value type) {
        super("Foreach " + type.getFullName());
        this.type = type;

        input("Signal", SignalValue.INSTANCE);
        input("List", ListValue.get(type));
        output("Loop", SignalValue.INSTANCE);
        output("Current", type);
        output("Next", SignalValue.INSTANCE);

        loadJava(ForeachNode.class);
    }

    @FlowSignalInput("Signal")
    private static void signal() {
        List<Object> copy = new ArrayList<>(list());
        for (Object each : copy) {
            ctx().cpuCheck();
            ctx().setInternalVar("ID$current", each);
            loop();
        }
        next();
    }

    @FlowSignalOutput("Loop")
    private static void loop() {
        throw new IllegalStateException();
    }

    @FlowSignalOutput("Next")
    private static void next() {
        throw new IllegalStateException();
    }

    @FlowValueOutput("Current")
    private static Object current() {
        return ctx().getInternalVar("ID$current");
    }

    @FlowValueInput("List")
    private static List<Object> list() {
        throw new IllegalStateException();
    }

    @FlowContext
    private static CompiledNode ctx() {
        throw new IllegalStateException();
    }

    @Override
    public String getBaseName() {
        return "Foreach";
    }

    @Override
    public List<Value> generics() {
        return List.of(type);
    }

    @Override
    public Node fromGenerics(List<Value> generics) {
        return new ForeachNode(generics.getFirst());
    }

    @Override
    public List<Value.GenericParam> possibleGenerics(List<StructDefinition> structs) {
        return List.of(new Value.GenericParam("List Type", AllValues.dataOnly(structs)));
    }
}
