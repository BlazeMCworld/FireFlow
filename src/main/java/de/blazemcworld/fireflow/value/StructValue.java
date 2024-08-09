package de.blazemcworld.fireflow.value;

import de.blazemcworld.fireflow.compiler.NodeCompiler;
import de.blazemcworld.fireflow.compiler.instruction.Instruction;
import de.blazemcworld.fireflow.compiler.instruction.MultiInstruction;
import de.blazemcworld.fireflow.compiler.instruction.RawInstruction;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.format.TextColor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class StructValue implements Value {

    private static final WeakHashMap<String, StructValue> cache = new WeakHashMap<>();
    public static StructValue get(String name, List<Pair<String, Value>> types) {
        return cache.computeIfAbsent(name, n -> {
            StructValue type = new StructValue(name);
            type.types = new ArrayList<>(types);
            return type;
        });
    }

    public ArrayList<Pair<String, Value>> types = new ArrayList<>();

    private final String name;
    private StructValue(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TextColor getColor() {
        return null;
    }

    @Override
    public Type getType() {
        return Type.getType("[Ljava/lang/Object;");
    }

    @Override
    public InsnList compile(NodeCompiler ctx, Object inset) {
        if (inset != null) throw new IllegalStateException("Struct values can't be inset!");
        InsnList out = new InsnList();
        out.add(new IntInsnNode(Opcodes.BIPUSH, types.size()));
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
                        new IntInsnNode(Opcodes.BIPUSH, types.size()),
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
}
