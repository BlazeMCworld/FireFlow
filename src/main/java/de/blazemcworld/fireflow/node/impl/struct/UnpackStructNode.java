package de.blazemcworld.fireflow.node.impl.struct;

import de.blazemcworld.fireflow.compiler.instruction.MultiInstruction;
import de.blazemcworld.fireflow.compiler.instruction.RawInstruction;
import de.blazemcworld.fireflow.node.Node;
import de.blazemcworld.fireflow.node.NodeInput;
import de.blazemcworld.fireflow.value.StructValue;
import de.blazemcworld.fireflow.value.Value;
import it.unimi.dsi.fastutil.Pair;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

public class UnpackStructNode extends Node {

    public UnpackStructNode(StructValue type) {
        super("Unpack " + type.getName());
        NodeInput struct = input(type.getName(), type);
        for (int i = 0; i < type.types.size(); i++) {
            Pair<String, Value> pair = type.types.get(i);
            Value fieldType = pair.right();
            output(pair.left(), fieldType).setInstruction(new MultiInstruction(Type.getType("Ljava/lang/Object;"),
                    struct,
                    new RawInstruction(Type.VOID_TYPE, new LdcInsnNode(i)),
                    fieldType.cast(new RawInstruction(Type.VOID_TYPE, new InsnNode(Opcodes.AALOAD)))
            ));
        }
    }
}
