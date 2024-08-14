package de.blazemcworld.fireflow.node.impl.dictionary;

import de.blazemcworld.fireflow.compiler.StructDefinition;
import de.blazemcworld.fireflow.node.Node;
import de.blazemcworld.fireflow.node.annotation.FlowValueInput;
import de.blazemcworld.fireflow.node.annotation.FlowValueOutput;
import de.blazemcworld.fireflow.value.AllValues;
import de.blazemcworld.fireflow.value.DictionaryValue;
import de.blazemcworld.fireflow.value.ListValue;
import de.blazemcworld.fireflow.value.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DictionaryKeysNode extends Node {

    private final Value key;
    private final Value value;

    public DictionaryKeysNode(Value key, Value value) {
        super("Dictionary<" + key.getFullName() + ", " + value.getFullName() + "> Keys");

        this.key = key;
        this.value = value;

        input("Dictionary", DictionaryValue.get(key, value));
        output("Keys", ListValue.get(key));

        loadJava(DictionaryKeysNode.class);
    }

    @FlowValueOutput("Keys")
    private static List<?> keys() {
        return new ArrayList<>(dictionary().keySet());
    }

    @FlowValueInput("Dictionary")
    private static Map<Object, Object> dictionary() {
        throw new IllegalStateException();
    }

    @Override
    public String getBaseName() {
        return "Dictionary Keys";
    }

    @Override
    public List<Value> generics() {
        return List.of(key, value);
    }

    @Override
    public Node fromGenerics(List<Value> generics) {
        return new DictionaryKeysNode(generics.getFirst(), generics.get(1));
    }

    @Override
    public List<Value.GenericParam> possibleGenerics(List<StructDefinition> structs) {
        return List.of(
                new Value.GenericParam("Key Type", AllValues.dataOnly(structs)),
                new Value.GenericParam("Value Type", AllValues.dataOnly(structs))
        );
    }

}
