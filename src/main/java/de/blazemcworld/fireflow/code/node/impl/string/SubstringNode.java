package de.blazemcworld.fireflow.code.node.impl.string;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.StringType;
import net.minecraft.item.Items;

public class SubstringNode extends Node {
    public SubstringNode() {
        super("substring", "Substring", "Returns a portion of a string", Items.PAPER);

        Input<String> input = new Input<>("input", "Input", StringType.INSTANCE);
        Input<Double> start = new Input<>("start", "Start", NumberType.INSTANCE);
        Input<Double> end = new Input<>("end", "End", NumberType.INSTANCE);
        Output<String> substring = new Output<>("substring", "Substring", StringType.INSTANCE);

        substring.valueFrom(ctx -> {
            String str = input.getValue(ctx);
            int s = start.getValue(ctx).intValue();
            int e = end.getValue(ctx).intValue();
            if (s < 0) s = 0;
            if (e > str.length()) e = str.length();
            return str.substring(s, e);
        });
    }

    @Override
    public Node copy() {
        return new SubstringNode();
    }
}
