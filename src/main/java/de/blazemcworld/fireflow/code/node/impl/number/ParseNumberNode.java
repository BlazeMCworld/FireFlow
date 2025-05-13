package de.blazemcworld.fireflow.code.node.impl.number;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.StringType;
import net.minecraft.item.Items;

public class ParseNumberNode extends Node {
    
    public ParseNumberNode() {
        super("parse_number", "Parse Number", "Converts a string to a number", Items.IRON_INGOT);

        Input<String> string = new Input<>("string", "String", StringType.INSTANCE);
        Output<Double> number = new Output<>("number", "Number", NumberType.INSTANCE);

        number.valueFrom((ctx) -> {
            Double parsed = NumberType.INSTANCE.parseInset(string.getValue(ctx));
            if (parsed == null) return 0.0;
            return parsed;
        });
    }

    @Override
    public Node copy() {
        return new ParseNumberNode();
    }

}
