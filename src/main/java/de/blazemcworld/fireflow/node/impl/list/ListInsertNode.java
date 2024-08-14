package de.blazemcworld.fireflow.node.impl.list;

import de.blazemcworld.fireflow.compiler.StructDefinition;
import de.blazemcworld.fireflow.node.Node;
import de.blazemcworld.fireflow.node.annotation.FlowSignalInput;
import de.blazemcworld.fireflow.node.annotation.FlowSignalOutput;
import de.blazemcworld.fireflow.node.annotation.FlowValueInput;
import de.blazemcworld.fireflow.value.*;

import java.util.List;

public class ListInsertNode extends Node {

    private final Value type;

    public ListInsertNode(Value type) {
        super("List<" + type.getFullName() + "> Insert");
        this.type = type;

        input("Signal", SignalValue.INSTANCE);
        input("List", ListValue.get(type));
        input("Index", NumberValue.INSTANCE);
        input("Value", type);
        output("Next", SignalValue.INSTANCE);

        loadJava(ListInsertNode.class);
    }

    @FlowSignalInput("Signal")
    private static void run() {
        list().add(Math.max(0, Math.min((int) index(), list().size())), value());
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
        return "List Insert";
    }

    @Override
    public List<Value> generics() {
        return List.of(type);
    }

    @Override
    public Node fromGenerics(List<Value> generics) {
        return new ListInsertNode(generics.getFirst());
    }

    @Override
    public List<Value.GenericParam> possibleGenerics(List<StructDefinition> structs) {
        return List.of(new Value.GenericParam("List Type", AllValues.dataOnly(structs)));
    }
}
