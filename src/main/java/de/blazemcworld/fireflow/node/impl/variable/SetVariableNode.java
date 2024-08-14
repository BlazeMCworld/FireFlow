package de.blazemcworld.fireflow.node.impl.variable;

import de.blazemcworld.fireflow.compiler.StructDefinition;
import de.blazemcworld.fireflow.compiler.instruction.DiscardInstruction;
import de.blazemcworld.fireflow.compiler.instruction.InstanceMethodInstruction;
import de.blazemcworld.fireflow.compiler.instruction.MultiInstruction;
import de.blazemcworld.fireflow.node.Node;
import de.blazemcworld.fireflow.node.NodeInput;
import de.blazemcworld.fireflow.node.NodeOutput;
import de.blazemcworld.fireflow.value.AllValues;
import de.blazemcworld.fireflow.value.SignalValue;
import de.blazemcworld.fireflow.value.TextValue;
import de.blazemcworld.fireflow.value.Value;
import it.unimi.dsi.fastutil.Pair;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.Map;

public class SetVariableNode extends Node {

    private final Value type;
    private final VariableScope scope;

    public SetVariableNode(VariableScope scope, Value type) {
        super("Set " + scope.getName() + " " + type.getFullName() + " Variable");
        this.type = type;
        this.scope = scope;

        NodeInput signal = input("Signal", SignalValue.INSTANCE);
        NodeInput name = input("Name", TextValue.INSTANCE);
        NodeInput value = input("Value", type);
        NodeOutput next = output("Next", SignalValue.INSTANCE);

        signal.setInstruction(new MultiInstruction(type.getType(),
                new DiscardInstruction(new InstanceMethodInstruction(Map.class, scope.getStore(), "put",
                        Type.getType(Object.class), List.of(
                            Pair.of(Type.getType(Object.class), name),
                            Pair.of(Type.getType(Object.class), type.wrapPrimitive(value))
                        )
                )),
                next
        ));
    }

    @Override
    public String getBaseName() {
        return "Set " + scope.getName() + " Variable";
    }

    @Override
    public List<Value> generics() {
        return List.of(type);
    }

    @Override
    public Node fromGenerics(List<Value> generics) {
        return new SetVariableNode(scope, generics.getFirst());
    }

    @Override
    public List<Value.GenericParam> possibleGenerics(List<StructDefinition> structs) {
        return List.of(new Value.GenericParam("Variable Type", AllValues.dataOnly(structs)));
    }
}
