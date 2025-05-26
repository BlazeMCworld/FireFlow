package de.blazemcworld.fireflow.code.node.impl.string;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.StringType;
import net.minecraft.item.Items;

public class UppercaseNode extends Node {
    public UppercaseNode() {
        super("uppercase", "To Uppercase", "Makes a string uppercase", Items.IRON_BLOCK);

        Input<String> input = new Input<>("input", "Input", StringType.INSTANCE);
        Output<String> output = new Output<>("output", "Output", StringType.INSTANCE);

        output.valueFrom(ctx -> {
            return input.getValue(ctx).toUpperCase();
        });
    }

    @Override
    public Node copy() {
        return new UppercaseNode();
    }
}
