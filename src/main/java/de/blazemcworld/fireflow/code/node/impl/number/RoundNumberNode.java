package de.blazemcworld.fireflow.code.node.impl.number;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.StringType;
import net.minecraft.item.Items;

public class RoundNumberNode extends Node {
    public RoundNumberNode() {
        super("round_number", "Round Number", "Rounds a number according to specified mode and decimal places", Items.DISPENSER);
        Input<Double> value = new Input<>("value", "Value", NumberType.INSTANCE);
        Input<String> mode = new Input<>("mode", "Mode", StringType.INSTANCE).options("Round", "Floor", "Ceiling");
        Input<Double> decimal_place = new Input<>("decimal_place", "Decimal Place", NumberType.INSTANCE);
        Output<Double> result = new Output<>("result", "Result", NumberType.INSTANCE);

        result.valueFrom((ctx) -> {
            double decimal = decimal_place.getValue(ctx);
            decimal = Math.pow(10, decimal);
            double valueInput = value.getValue(ctx);
            switch (mode.getValue(ctx)) {
                case "Round" -> {
                    return Math.round(valueInput / decimal) * decimal;
                }
                case "Floor" -> {
                    return Math.floor(valueInput / decimal) * decimal;
                }
                case "Ceiling" -> {
                    return Math.ceil(valueInput / decimal) * decimal;
                }
                default -> {
                    return 0.0;
                }
            }
        });
    }

    @Override
    public Node copy() {
        return new RoundNumberNode();
    }
}