package de.blazemcworld.fireflow.node.impl.list;

import de.blazemcworld.fireflow.compiler.StructDefinition;
import de.blazemcworld.fireflow.node.Node;
import de.blazemcworld.fireflow.node.annotation.FlowValueOutput;
import de.blazemcworld.fireflow.value.AllValues;
import de.blazemcworld.fireflow.value.ListValue;
import de.blazemcworld.fireflow.value.Value;

import java.util.ArrayList;
import java.util.List;

public class EmptyListNode extends Node {

    private final Value type;

    public EmptyListNode(Value type) {
        super("Empty List<" + type.getFullName() + ">");
        this.type = type;

        output("List", ListValue.get(type));

        loadJava(EmptyListNode.class);
    }

    @FlowValueOutput("List")
    private static List<Object> list() {
        return new ArrayList<>();
    }

    @Override
    public String getBaseName() {
        return "Empty List";
    }

    @Override
    public Node fromGenerics(List<Value> generics) {
        return new EmptyListNode(generics.getFirst());
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
