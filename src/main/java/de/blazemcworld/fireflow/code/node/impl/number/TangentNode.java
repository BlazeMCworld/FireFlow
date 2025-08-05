package de.blazemcworld.fireflow.code.node.impl.number;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.StringType;
import org.bukkit.Material;

public class TangentNode extends Node {
    public TangentNode() {
        super("tangent", "Tangent", "Generates the trigonometric tangent function of a number.", Material.HORN_CORAL_FAN);
        Input<Double> value = new Input<>("value", "Value", NumberType.INSTANCE);
        Input<String> mode = new Input<>("mode", "Mode", StringType.INSTANCE).options("tan","tanh","atan");
        Input<String> inputUnit = new Input<>("input_unit", "Input Unit", StringType.INSTANCE).options("Degrees","Radians");
        Output<Double> result = new Output<>("result", "Result", NumberType.INSTANCE);

        result.valueFrom((ctx) -> {
            double valueInput = value.getValue(ctx);
            switch (mode.getValue(ctx)) {
                case "tan" -> {
                    if (inputUnit.getValue(ctx).equals("Degrees")) {
                        return Math.tan(Math.toRadians(valueInput));
                    } else {
                        return Math.tan(valueInput);
                    }
                }
                case "atan" -> {
                    if (inputUnit.getValue(ctx).equals("Degrees")) {
                        return Math.toDegrees(Math.atan(valueInput));
                    } else {
                        return Math.atan(valueInput);
                    }
                }
                case "tanh" -> {
                    return Math.tanh(valueInput);
                }
                default -> {
                    return 0.0;
                }
            }
        });
    }

    @Override
    public Node copy() {
        return new TangentNode();
    }
}