package de.blazemcworld.fireflow.node.impl.variable;

import de.blazemcworld.fireflow.compiler.StructDefinition;
import de.blazemcworld.fireflow.compiler.instruction.InstanceMethodInstruction;
import de.blazemcworld.fireflow.compiler.instruction.MultiInstruction;
import de.blazemcworld.fireflow.node.Node;
import de.blazemcworld.fireflow.node.NodeInput;
import de.blazemcworld.fireflow.node.NodeOutput;
import de.blazemcworld.fireflow.value.AllValues;
import de.blazemcworld.fireflow.value.TextValue;
import de.blazemcworld.fireflow.value.Value;
import it.unimi.dsi.fastutil.Pair;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.Map;

public class GetVariableNode extends Node {

    private final Value type;
    private final VariableScope scope;

    public GetVariableNode(VariableScope scope, Value type) {
        super("Get " + scope.getName() + " " + type.getFullName() + " Variable");
        this.scope = scope;
        this.type = type;

        NodeInput name = input("Name", TextValue.INSTANCE);
        NodeOutput value = output("Value", type);

        value.setInstruction(new MultiInstruction(type.getType(),
                type.cast(new InstanceMethodInstruction(Map.class, scope.getStore(), "get",
                        Type.getType(Object.class),
                        List.of(Pair.of(Type.getType(Object.class), name))
                ))
        ));
    }

    @Override
    public String getBaseName() {
        return "Get " + scope.getName() + " Variable";
    }

    @Override
    public List<Value> generics() {
        return List.of(type);
    }

    @Override
    public Node fromGenerics(List<Value> generics) {
        return new GetVariableNode(scope, generics.getFirst());
    }

    @Override
    public List<Value.GenericParam> possibleGenerics(List<StructDefinition> structs) {
        return List.of(new Value.GenericParam("Variable Type", AllValues.dataOnly(structs)));
    }
}
