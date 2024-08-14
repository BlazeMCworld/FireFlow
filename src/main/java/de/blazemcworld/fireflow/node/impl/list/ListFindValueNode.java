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

public class ListFindValueNode extends Node {

    private final Value type;

    public ListFindValueNode(Value type) {
        super("List<" + type.getFullName() + "> Find Value");
        this.type = type;

        input("List", ListValue.get(type));
        input("Value", type);

        output("Index", NumberValue.INSTANCE);

        loadJava(ListFindValueNode.class);
    }

    @FlowValueOutput("Index")
    private static double index() {
        return list().indexOf(value());
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
        return "List Find";
    }

    @Override
    public Node fromGenerics(List<Value> generics) {
        return new ListFindValueNode(generics.getFirst());
    }

    @Override
    public List<Value> generics() {
        return List.of(type);
    }

    @Override
    public List<Value.GenericParam> possibleGenerics(List<StructDefinition> structs) {
        return List.of(new Value.GenericParam("List Type", AllValues.dataOnly(structs)));
    }
}
