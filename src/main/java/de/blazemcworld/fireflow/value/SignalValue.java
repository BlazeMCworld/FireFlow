package de.blazemcworld.fireflow.value;

import de.blazemcworld.fireflow.compiler.NodeCompiler;
import de.blazemcworld.fireflow.compiler.instruction.Instruction;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;

public class SignalValue implements Value {
    public static SignalValue INSTANCE = new SignalValue();
    private SignalValue() {}

    @Override
    public String getName() {
        return "Signal";
    }

    @Override
    public TextColor getColor() {
        return NamedTextColor.AQUA;
    }

    @Override
    public Type getType() {
        return Type.VOID_TYPE;
    }

    @Override
    public InsnList compile(NodeCompiler ctx, Object inset) {
        throw new IllegalStateException("Signal inputs can not be used as instructions!");
    }

    @Override
    public Instruction cast(Instruction value) {
        throw new IllegalStateException("Signal inputs can not be used as instructions!");
    }

    @Override
    public Instruction wrapPrimitive(Instruction value) {
        throw new IllegalStateException("Signal inputs can not be used as instructions!");
    }
}