package de.blazemcworld.fireflow.node.impl.list;

import de.blazemcworld.fireflow.compiler.StructDefinition;
import de.blazemcworld.fireflow.node.Node;
import de.blazemcworld.fireflow.node.annotation.FlowValueInput;
import de.blazemcworld.fireflow.node.annotation.FlowValueOutput;
import de.blazemcworld.fireflow.value.AllValues;
import de.blazemcworld.fireflow.value.ListValue;
import de.blazemcworld.fireflow.value.Value;

import java.util.List;

public class RandomListValueNode extends Node {

    private final Value type;

    public RandomListValueNode(Value type) {
        super("Random List<" + type.getFullName() + "> Value");
        this.type = type;

        input("List", ListValue.get(type));
        output("Value", type);

        loadJava(RandomListValueNode.class);
    }

    @FlowValueInput("List")
    private static List<Object> list() {
        throw new IllegalStateException();
    }

    @FlowValueOutput("Value")
    private static Object value() {
        List<Object> list = list();
        Object out = null;
        if (!list.isEmpty()) {
            out = list.get((int) (Math.random() * list.size()));
        }
        return out;
    }

    @Override
    public String getBaseName() {
        return "Random List Value";
    }

    @Override
    public Node fromGenerics(List<Value> generics) {
        return new RandomListValueNode(generics.getFirst());
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
