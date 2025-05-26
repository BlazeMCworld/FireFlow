package de.blazemcworld.fireflow.code.node.impl.string;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.StringType;
import net.minecraft.item.Items;

public class LowercaseNode extends Node {
    public LowercaseNode() {
        super("lowercase", "To Lowercase", "Makes a string lowercase", Items.IRON_NUGGET);

        Input<String> input = new Input<>("input", "Input", StringType.INSTANCE);
        Output<String> output = new Output<>("output", "Output", StringType.INSTANCE);

        output.valueFrom(ctx -> {
            return input.getValue(ctx).toLowerCase();
        });
    }

    @Override
    public Node copy() {
        return new LowercaseNode();
    }
}
