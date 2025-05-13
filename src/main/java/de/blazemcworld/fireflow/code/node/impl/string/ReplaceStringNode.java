package de.blazemcworld.fireflow.code.node.impl.string;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.StringType;
import net.minecraft.item.Items;

public class ReplaceStringNode extends Node {
    public ReplaceStringNode() {
        super("replace_string", "Replace String", "Replaces occurrences of a substring within a string", Items.NAME_TAG);

        Input<String> input = new Input<>("input", "Input", StringType.INSTANCE);
        Input<String> old = new Input<>("old", "Old", StringType.INSTANCE);
        Input<String> newStr = new Input<>("new", "New", StringType.INSTANCE);
        Output<String> result = new Output<>("result", "Result", StringType.INSTANCE);

        result.valueFrom(ctx -> input.getValue(ctx).replace(old.getValue(ctx), newStr.getValue(ctx)));
    }

    @Override
    public Node copy() {
        return new ReplaceStringNode();
    }
}
