package de.blazemcworld.fireflow.code.node.impl.string;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ListType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.value.ListValue;
import net.minecraft.item.Items;

import java.util.List;

public class SplitStringNode extends Node {
    public SplitStringNode() {
        super("split_string", "Split String", "Splits a string into an list based on a separating string", Items.SHEARS);

        Input<String> inputString = new Input<>("inputString", "Input String", StringType.INSTANCE);
        Input<String> separator = new Input<>("separator", "Separator", StringType.INSTANCE);
        Output<ListValue<String>> result = new Output<>("result", "Result", ListType.of(StringType.INSTANCE));

        result.valueFrom(ctx -> new ListValue<>(StringType.INSTANCE, List.of(
                inputString.getValue(ctx).split(separator.getValue(ctx))
        )));
    }

    @Override
    public Node copy() {
        return new SplitStringNode();
    }
}
