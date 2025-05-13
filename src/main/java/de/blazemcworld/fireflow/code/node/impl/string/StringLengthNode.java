package de.blazemcworld.fireflow.code.node.impl.string;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.StringType;
import net.minecraft.item.Items;

public class StringLengthNode extends Node {
    public StringLengthNode() {
        super("string_length", "String Length", "Returns the length of a string", Items.STICK);

        Input<String> string = new Input<>("string", "String", StringType.INSTANCE);
        Output<Double> length = new Output<>("length", "Length", NumberType.INSTANCE);

        length.valueFrom(ctx -> (double) string.getValue(ctx).length());
    }

    @Override
    public Node copy() {
        return new StringLengthNode();
    }
}