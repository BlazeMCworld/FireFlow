package de.blazemcworld.fireflow.node.impl.list;

import de.blazemcworld.fireflow.compiler.StructDefinition;
import de.blazemcworld.fireflow.node.Node;
import de.blazemcworld.fireflow.node.annotation.FlowValueInput;
import de.blazemcworld.fireflow.node.annotation.FlowValueOutput;
import de.blazemcworld.fireflow.value.AllValues;
import de.blazemcworld.fireflow.value.ListValue;
import de.blazemcworld.fireflow.value.NumberValue;
import de.blazemcworld.fireflow.value.Value;

import java.util.List;

public class ListGetNode extends Node {

    private final Value type;

    public ListGetNode(Value type) {
        super("List<" + type.getFullName() + "> Get");
        this.type = type;

        input("List", ListValue.get(type));
        input("Index", NumberValue.INSTANCE);
        output("Value", type);

        loadJava(ListGetNode.class);
    }

    @FlowValueOutput("Value")
    private static Object value() {
        return list().get((int) index());
    }

    @FlowValueInput("List")
    private static List<Object> list() {
        throw new IllegalStateException();
    }

    @FlowValueInput("Index")
    private static double index() {
        throw new IllegalStateException();
    }

    @Override
    public String getBaseName() {
        return "List Get";
    }

    @Override
    public List<Value> generics() {
        return List.of(type);
    }

    @Override
    public Node fromGenerics(List<Value> generics) {
        return new ListGetNode(generics.getFirst());
    }

    @Override
    public List<Value.GenericParam> possibleGenerics(List<StructDefinition> structs) {
        return List.of(new Value.GenericParam("List Type", AllValues.dataOnly(structs)));
    }
}
