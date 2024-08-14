package de.blazemcworld.fireflow.compiler;

import de.blazemcworld.fireflow.compiler.instruction.Instruction;
import de.blazemcworld.fireflow.compiler.instruction.MultiInstruction;
import de.blazemcworld.fireflow.compiler.instruction.RawInstruction;
import de.blazemcworld.fireflow.node.Node;
import de.blazemcworld.fireflow.node.NodeInput;
import de.blazemcworld.fireflow.node.NodeOutput;
import de.blazemcworld.fireflow.value.SignalValue;
import de.blazemcworld.fireflow.value.StructValue;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.ArrayList;
import java.util.List;

public final class StructDefinition {

    public final StructValue type;
    public final String stName;
    public final InitializationNode initNode;
    private final FunctionDefinition initializer;

    public StructDefinition(StructValue type) {
        this.type = type;
        this.stName = type.getBaseName().replace(" Struct", "");
        ArrayList<NodeOutput> fnInputs = new ArrayList<>(type.fields.size() + 1);
        fnInputs.add(new NodeOutput("On Creation", SignalValue.INSTANCE));
        for (int i = 0; i < type.fields.size(); i++) {
            StructValue.Field field = type.fields.get(i);
            fnInputs.add(new NodeOutput(field.name(), field.type()));
        }
        initializer = new FunctionDefinition(stName + " Initializer", fnInputs, List.of()); //TODO: wait for optional node inputs
        initNode = new InitializationNode();
    }

    public StructDefinition.Create createCall() {
        return new Create();
    }

    public class InitializationNode extends Node {
        public final FunctionDefinition.DefinitionNode fnInputsNode;
        public InitializationNode() {
            super(stName + " Initializer");
            this.fnInputsNode = StructDefinition.this.initializer.fnInputsNode;
            outputs = fnInputsNode.outputs;
        }

        public StructDefinition getDefinition() {
            return StructDefinition.this;
        }

        @Override
        public String getBaseName() {
            return stName;
        }
    }

    public class Create extends Node {
        public final FunctionDefinition.Call funcCall;
        public Create() {
            super(stName);

            int size = initializer.fnInputs.size() - 1;
            ArrayList<NodeOutput> createInputs = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                NodeOutput in = initializer.fnInputs.get(i + 1);
                createInputs.add(new NodeOutput(in.getName(), in.type));
            }

            FunctionDefinition.Call defCall = initializer.createCall();
            NodeOutput struct = new NodeOutput(stName, type);
            Instruction[] instructions = new Instruction[size + 2];
            instructions[0] = defCall.inputs.getFirst();
            instructions[1] = new RawInstruction(type.getType(),
                    new IntInsnNode(Opcodes.BIPUSH, size),
                    new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object")
            );
            for (int i = 0; i < size; i++) {
                NodeOutput out = createInputs.get(i);
                defCall.inputs.get(i + 1).connectValue(out);
                instructions[i + 2] = new MultiInstruction(Type.VOID_TYPE,
                        new RawInstruction(Type.VOID_TYPE,
                                new InsnNode(Opcodes.DUP),
                                new LdcInsnNode(i)
                        ),
                        out.getType().wrapPrimitive(out),
                        new RawInstruction(Type.VOID_TYPE, new InsnNode(Opcodes.AASTORE))
                );
            }

            struct.setInstruction(new MultiInstruction(type.getType(), instructions));
            NodeInput createOut = new NodeInput(stName, type);
            createOut.connectValue(struct);

            funcCall = new FunctionDefinition("Create " + stName, createInputs, List.of(createOut)).createCall();
            inputs = funcCall.inputs;
            outputs = funcCall.outputs;
        }

        public StructDefinition getDefinition() {
            return StructDefinition.this;
        }
    }
}
