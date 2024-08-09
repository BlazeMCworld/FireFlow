package de.blazemcworld.fireflow.node.impl.struct;

import de.blazemcworld.fireflow.compiler.instruction.Instruction;
import de.blazemcworld.fireflow.compiler.instruction.MultiInstruction;
import de.blazemcworld.fireflow.compiler.instruction.RawInstruction;
import de.blazemcworld.fireflow.node.Node;
import de.blazemcworld.fireflow.node.NodeInput;
import de.blazemcworld.fireflow.node.NodeOutput;
import de.blazemcworld.fireflow.value.SignalValue;
import de.blazemcworld.fireflow.value.StructValue;
import de.blazemcworld.fireflow.value.Value;
import it.unimi.dsi.fastutil.Pair;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.WeakHashMap;

public class StructNode extends Node {

    public final StructValue type;

    private static final WeakHashMap<StructValue, StructNode> cache = new WeakHashMap<>();
    public static StructNode get(StructValue type) {
        return cache.computeIfAbsent(type, StructNode::new);
    }

    private StructNode(StructValue type) {
        super(type.getName() + " Struct");
        this.type = type;
        for (Pair<String, Value> pair : type.types) output(pair.left(), pair.right());
    }

    @Override
    public NodeOutput output(String name, Value value) {
        if (value == SignalValue.INSTANCE) throw new IllegalArgumentException("no");
        if (!type.types.contains(Pair.of(name, value))) type.types.add(Pair.of(name, value));
        return super.output(name, value);
    }

    public CreateNode newCreateNode() {
        return new CreateNode();
    }

    public class CreateNode extends Node {

        public CreateNode() {
            super(StructNode.this.type.getName());
            output(name, type);
            inputs = StructNode.this.outputs.stream().map(o -> new NodeInput(o.getName(), o.getType())).toList();
        }

        public NodeOutput getOut() {
            int size = inputs.size();
            Instruction[] instructions = new Instruction[size + 1];
            instructions[0] = new RawInstruction(type.getType(),
                    new IntInsnNode(Opcodes.BIPUSH, size),
                    new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object")
            );
            for (int i = 1; i < size + 1; i++) {
                NodeInput input = inputs.get(i - 1);
                instructions[i] = new MultiInstruction(Type.VOID_TYPE,
                        new RawInstruction(Type.VOID_TYPE,
                                new InsnNode(Opcodes.DUP),
                                new LdcInsnNode(i - 1)
                        ),
                        input.getType().wrapPrimitive(input),
                        new RawInstruction(Type.VOID_TYPE, new InsnNode(Opcodes.AASTORE))
                );
            }
            NodeOutput out = outputs.getFirst();
            out.setInstruction(new MultiInstruction(type.getType(), instructions));
            return out;
        }
    }
}
