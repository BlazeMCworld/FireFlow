package de.blazemcworld.fireflow.node.impl.extraction.list;

import de.blazemcworld.fireflow.compiler.instruction.InstanceMethodInstruction;
import de.blazemcworld.fireflow.compiler.instruction.MultiInstruction;
import de.blazemcworld.fireflow.node.ExtractionNode;
import de.blazemcworld.fireflow.value.ListValue;
import de.blazemcworld.fireflow.value.NumberValue;
import org.objectweb.asm.Type;

import java.util.List;

public class ListSizeNode extends ExtractionNode {
    public ListSizeNode(ListValue type) {
        super(type.getFullName() + " Size", type, NumberValue.INSTANCE);
        output.setInstruction(new MultiInstruction(NumberValue.INSTANCE.getType(), NumberValue.INSTANCE.cast(
                new InstanceMethodInstruction(List.class, input, "size", Type.INT_TYPE, List.of())
        )));
    }
}
