package de.blazemcworld.fireflow.value;

import de.blazemcworld.fireflow.compiler.NodeCompiler;
import de.blazemcworld.fireflow.compiler.instruction.Instruction;
import de.blazemcworld.fireflow.compiler.instruction.MultiInstruction;
import de.blazemcworld.fireflow.compiler.instruction.RawInstruction;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.network.NetworkBuffer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;

public class StructValue implements Value {

    public final static StructValue UNKNOWN = new StructValue("UNKNOWN", new ArrayList<>());

    public final ArrayList<Field> fields;
    private final String name;
    public StructValue(String name, ArrayList<Field> fields) {
        if (fields.size() >= Byte.MAX_VALUE) throw new RuntimeException("Too many fields for struct " + name +"!");
        this.name = name;
        this.fields = fields;
    }

    @Override
    public String getBaseName() {
        return name + " Struct";
    }

    @Override
    public TextColor getColor() {
        return NamedTextColor.GOLD;
    }

    @Override
    public Type getType() {
        return Type.getType("[Ljava/lang/Object;");
    }

    @Override
    public InsnList compile(NodeCompiler ctx, Object inset) {
        if (inset != null) throw new IllegalStateException("Struct values can't be inset!");
        InsnList out = new InsnList();
        out.add(new IntInsnNode(Opcodes.BIPUSH, fields.size()));
        out.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"));
        return out;
    }

    @Override
    public Instruction cast(Instruction value) {
        LabelNode cast = new LabelNode();
        LabelNode end = new LabelNode();
        return new MultiInstruction(getType(),
                value,
                new RawInstruction(getType(),
                        new InsnNode(Opcodes.DUP),
                        new TypeInsnNode(Opcodes.INSTANCEOF, "[Ljava/lang/Object;"),
                        new JumpInsnNode(Opcodes.IFGT, cast),
                        new InsnNode(Opcodes.POP),
                        new IntInsnNode(Opcodes.BIPUSH, fields.size()),
                        new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"),
                        new JumpInsnNode(Opcodes.GOTO, end),
                        cast,
                        new TypeInsnNode(Opcodes.CHECKCAST, "[Ljava/lang/Object;"),
                        end
                )
        );
    }

    @Override
    public Instruction wrapPrimitive(Instruction value) {
        return value;
    }

    @Override
    public Object prepareInset(String message) {
        return null;
    }

    @Override
    public void writeInset(NetworkBuffer buffer, Object inset) {
        throw new IllegalStateException("Struct (" + name + ") values can not be inset!");
    }

    @Override
    public Object readInset(NetworkBuffer buffer) {
        throw new IllegalStateException("Struct (" + name + ") values can not be inset!");
    }

    public record Field(String name, Value type) {}
}
