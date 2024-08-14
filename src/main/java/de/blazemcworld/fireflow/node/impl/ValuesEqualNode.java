package de.blazemcworld.fireflow.node.impl;

import de.blazemcworld.fireflow.compiler.StructDefinition;
import de.blazemcworld.fireflow.node.Node;
import de.blazemcworld.fireflow.node.annotation.FlowValueInput;
import de.blazemcworld.fireflow.node.annotation.FlowValueOutput;
import de.blazemcworld.fireflow.value.AllValues;
import de.blazemcworld.fireflow.value.ConditionValue;
import de.blazemcworld.fireflow.value.Value;

import java.util.List;
import java.util.Objects;

public class ValuesEqualNode extends Node {

    private final Value type;

    public ValuesEqualNode(Value type) {
        super("Values Equal");
        this.type = type;

        input("Left", type);
        input("Right", type);
        output("Result", ConditionValue.INSTANCE);

        loadJava(ValuesEqualNode.class);
    }

    @FlowValueOutput("Result")
    private static boolean result() {
        return Objects.equals(left(), right());
    }

    @FlowValueInput("Left")
    private static Object left() {
        throw new IllegalStateException();
    }

    @FlowValueInput("Right")
    private static Object right() {
        throw new IllegalStateException();
    }

    @Override
    public String getBaseName() {
        return "Values Equal";
    }

    @Override
    public List<Value> generics() {
        return List.of(type);
    }

    @Override
    public Node fromGenerics(List<Value> generics) {
        return new ValuesEqualNode(generics.getFirst());
    }

    @Override
    public List<Value.GenericParam> possibleGenerics(List<StructDefinition> structs) {
        return List.of(new Value.GenericParam("Type", AllValues.dataOnly(structs)));
    }
}
