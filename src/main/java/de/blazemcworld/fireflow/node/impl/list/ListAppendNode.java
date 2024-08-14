package de.blazemcworld.fireflow.node.impl.list;

import de.blazemcworld.fireflow.compiler.StructDefinition;
import de.blazemcworld.fireflow.node.Node;
import de.blazemcworld.fireflow.node.annotation.FlowSignalInput;
import de.blazemcworld.fireflow.node.annotation.FlowSignalOutput;
import de.blazemcworld.fireflow.node.annotation.FlowValueInput;
import de.blazemcworld.fireflow.value.AllValues;
import de.blazemcworld.fireflow.value.ListValue;
import de.blazemcworld.fireflow.value.SignalValue;
import de.blazemcworld.fireflow.value.Value;

import java.util.List;

public class ListAppendNode extends Node {

    private final Value type;

    public ListAppendNode(Value type) {
        super("Append to List<" + type.getFullName() + ">");
        this.type = type;

        ListValue listType = ListValue.get(type);
        input("Signal", SignalValue.INSTANCE);
        input("List", listType);
        input("Value", type);
        output("Next", SignalValue.INSTANCE);

        loadJava(ListAppendNode.class);
    }

    @FlowSignalInput("Signal")
    private static void run() {
        list().add(value());
        next();
    }

    @FlowSignalOutput("Next")
    private static void next() {
        throw new IllegalStateException();
    }

    @FlowValueInput("List")
    private static List<Object> list() {
        throw new IllegalStateException();
    }

    @FlowValueInput("Value")
    private static Object value() {
        throw new IllegalStateException();
    }

    @Override
    public String getBaseName() {
        return "List Append";
    }

    @Override
    public List<Value> generics() {
        return List.of(type);
    }

    @Override
    public List<Value.GenericParam> possibleGenerics(List<StructDefinition> structs) {
        return List.of(new Value.GenericParam("List Type", AllValues.dataOnly(structs)));
    }

    @Override
    public Node fromGenerics(List<Value> generics) {
        return new ListAppendNode(generics.getFirst());
    }
}
