package de.blazemcworld.fireflow.value;

import de.blazemcworld.fireflow.compiler.NodeCompiler;
import de.blazemcworld.fireflow.compiler.instruction.Instruction;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.network.NetworkBuffer;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;

import java.util.List;
import java.util.stream.Collectors;

public interface Value {
    String getBaseName();
    TextColor getColor();
    Type getType();
    InsnList compile(NodeCompiler ctx, Object inset);
    Instruction cast(Instruction value);
    Instruction wrapPrimitive(Instruction value);
    Object prepareInset(String message);
    void writeInset(NetworkBuffer buffer, Object inset);
    Object readInset(NetworkBuffer buffer);

    default String formatInset(Object inset) {
        return String.valueOf(inset);
    }

    default Value fromGenerics(List<Value> generics) {
        return this;
    }

    default List<Value> toGenerics() {
        return List.of();
    }

    default List<GenericParam> possibleGenerics() {
        return List.of();
    }

    default String getFullName() {
        String name = getBaseName();
        List<Value> generics = toGenerics();
        if (!generics.isEmpty()) {
            name += "<" + generics.stream().map(Value::getFullName).collect(Collectors.joining(", ")) + ">";
        }
        return name;
    }

    record GenericParam(String name, List<Value> possibleTypes) {}
}
