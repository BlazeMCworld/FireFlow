package de.blazemcworld.fireflow.node.impl.extraction.struct;

import de.blazemcworld.fireflow.compiler.instruction.MultiInstruction;
import de.blazemcworld.fireflow.compiler.instruction.RawInstruction;
import de.blazemcworld.fireflow.node.ExtractionNode;
import de.blazemcworld.fireflow.value.StructValue;
import de.blazemcworld.fireflow.value.Value;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

public class StructFieldNode extends ExtractionNode {
    public StructFieldNode(StructValue type, int i, StructValue.Field field) {
        super(field.name(), type, field.type());
        Value fieldType = field.type();
        output.setInstruction(new MultiInstruction(fieldType.getType(),
                input,
                new RawInstruction(Type.VOID_TYPE, new LdcInsnNode(i)),
                fieldType.cast(new RawInstruction(Type.VOID_TYPE, new InsnNode(Opcodes.AALOAD)))
        ));
    }
}
