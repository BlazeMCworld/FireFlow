package de.blazemcworld.fireflow.code.node.impl.function;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.WireType;
import net.minecraft.item.Item;

import java.util.HashSet;
import java.util.Set;

public class FunctionDefinition {
    
    public final Item icon;
    public final String name;
    public final Set<FunctionCallNode> callNodes = new HashSet<>();
    public final FunctionInputsNode inputsNode;
    public final FunctionOutputsNode outputsNode;

    public FunctionDefinition(String name, Item icon) {
        this.name = name;
        this.icon = icon;
        inputsNode = new FunctionInputsNode(this);
        outputsNode = new FunctionOutputsNode(this);
    }

    public void addInput(String name, WireType<?> type) {
        inputsNode.addInput(name, type);
    }

    public void addOutput(String name, WireType<?> type) {
        outputsNode.addOutput(name, type);
    }

    public Node.Output<?> getInput(String name) {
        for (Node.Output<?> output : inputsNode.outputs) {
            if (output.id.equals(name)) {
                return output;
            }
        }
        return null;
    }

    public Node.Input<?> getOutput(String name) {
        for (Node.Input<?> input : outputsNode.inputs) {
            if (input.id.equals(name)) {
                return input;
            }
        }
        return null;
    }

}
