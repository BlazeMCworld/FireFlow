package de.blazemcworld.fireflow.node.impl.dictionary;

import de.blazemcworld.fireflow.compiler.StructDefinition;
import de.blazemcworld.fireflow.node.Node;
import de.blazemcworld.fireflow.node.annotation.FlowValueOutput;
import de.blazemcworld.fireflow.value.AllValues;
import de.blazemcworld.fireflow.value.DictionaryValue;
import de.blazemcworld.fireflow.value.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmptyDictionaryNode extends Node {

    private final Value key;
    private final Value value;

    public EmptyDictionaryNode(Value key, Value value) {
        super("Empty Dictionary<" + key.getFullName() + ", " + value.getFullName() + ">");
        this.key = key;
        this.value = value;

        output("Dictionary", DictionaryValue.get(key, value));

        loadJava(EmptyDictionaryNode.class);
    }

    @FlowValueOutput("Dictionary")
    private static Map<Object, Object> dictionary() {
        return new HashMap<>();
    }

    @Override
    public String getBaseName() {
        return "Empty Dictionary";
    }

    @Override
    public Node fromGenerics(List<Value> generics) {
        return new EmptyDictionaryNode(generics.getFirst(), generics.get(1));
    }

    @Override
    public List<Value> generics() {
        return List.of(key, value);
    }

    @Override
    public List<Value.GenericParam> possibleGenerics(List<StructDefinition> structs) {
        return List.of(
                new Value.GenericParam("Key Type", AllValues.dataOnly(structs)),
                new Value.GenericParam("Value Type", AllValues.dataOnly(structs))
        );
    }

}
