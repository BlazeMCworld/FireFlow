package de.blazemcworld.fireflow.node.impl.list;

import de.blazemcworld.fireflow.compiler.StructDefinition;
import de.blazemcworld.fireflow.node.Node;
import de.blazemcworld.fireflow.node.annotation.FlowSignalInput;
import de.blazemcworld.fireflow.node.annotation.FlowSignalOutput;
import de.blazemcworld.fireflow.node.annotation.FlowValueInput;
import de.blazemcworld.fireflow.value.*;

import java.util.List;

public class ListSetNode extends Node {

    private final Value type;

    public ListSetNode(Value type) {
        super("List<" + type.getFullName() + "> Set");
        this.type = type;

        input("Signal", SignalValue.INSTANCE);
        input("List", ListValue.get(type));
        input("Index", NumberValue.INSTANCE);
        input("Value", type);
        output("Next", SignalValue.INSTANCE);

        loadJava(ListSetNode.class);
    }

    @FlowSignalOutput("Next")
    private static void next() {
        throw new IllegalStateException();
    }

    @FlowSignalInput("Signal")
    private static void signal() {
        List<Object> l = list();
        l.set(Math.max(0, Math.min(l.size() - 1, (int) index())), value());
        next();
    }

    @FlowValueInput("List")
    private static List<Object> list() {
        throw new IllegalStateException();
    }

    @FlowValueInput("Index")
    private static double index() {
        throw new IllegalStateException();
    }

    @FlowValueInput("Value")
    private static Object value() {
        throw new IllegalStateException();
    }

    @Override
    public String getBaseName() {
        return "List Set";
    }

    @Override
    public List<Value> generics() {
        return List.of(type);
    }

    @Override
    public Node fromGenerics(List<Value> generics) {
        return new ListSetNode(generics.getFirst());
    }

    @Override
    public List<Value.GenericParam> possibleGenerics(List<StructDefinition> structs) {
        return List.of(new Value.GenericParam("List Type", AllValues.dataOnly(structs)));
    }
}
