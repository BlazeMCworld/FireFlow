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

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class DictionaryValue implements Value {

    private static final WeakHashMap<Value, WeakHashMap<Value, DictionaryValue>> cache = new WeakHashMap<>();

    private final Value keyType;
    private final Value valueType;

    private DictionaryValue(Value keyType, Value valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
    }

    public static DictionaryValue get(Value keyType, Value valueType) {
        return cache.computeIfAbsent(keyType, k -> new WeakHashMap<>()).computeIfAbsent(valueType, v -> new DictionaryValue(keyType, valueType));
    }

    @Override
    public String getBaseName() {
        return "Dictionary";
    }

    @Override
    public TextColor getColor() {
        return NamedTextColor.DARK_GREEN;
    }

    @Override
    public Type getType() {
        return Type.getType(Map.class);
    }

    @Override
    public InsnList compile(NodeCompiler ctx, Object inset) {
        if (inset != null) throw new IllegalStateException("Dictionary values can not be inset!");
        InsnList out = new InsnList();
        out.add(new TypeInsnNode(Opcodes.NEW, "java/util/HashMap"));
        out.add(new InsnNode(Opcodes.DUP));
        out.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false));
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
                        new TypeInsnNode(Opcodes.INSTANCEOF, "java/util/Map"),
                        new JumpInsnNode(Opcodes.IFGT, cast),
                        new InsnNode(Opcodes.POP),
                        new TypeInsnNode(Opcodes.NEW, "java/util/HashMap"),
                        new InsnNode(Opcodes.DUP),
                        new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false),
                        new JumpInsnNode(Opcodes.GOTO, end),
                        cast,
                        new TypeInsnNode(Opcodes.CHECKCAST, "java/util/Map"),
                        end
                 )
         );
    }

    @Override
    public Instruction wrapPrimitive(Instruction value) {
        return value;
    }

    public Object readInset(NetworkBuffer buffer) {
        throw new IllegalStateException("Dictionary values can not be inset!");
    }

    @Override
    public void writeInset(NetworkBuffer buffer, Object inset) {
        throw new IllegalStateException("Dictionary values can not be inset!");
    }

    @Override
    public Object prepareInset(String message) {
        return null;
    }

    public Value fromGenerics(List<Value> generics) {
        return DictionaryValue.get(generics.getFirst(), generics.getLast());
    }

    @Override
    public List<Value> toGenerics() {
        return List.of(keyType, valueType);
    }

    public List<GenericParam> possibleGenerics() {
        return List.of(new GenericParam("Key Type", AllValues.dataOnly), new GenericParam("Value Type", AllValues.dataOnly));
    }

}
