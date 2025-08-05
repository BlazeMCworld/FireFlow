package de.blazemcworld.fireflow.code.node.impl.number;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.StringType;
import org.bukkit.Material;

public class CosineNode extends Node {
    public CosineNode() {
        super("cosine", "Cosine", "Generates the trigonometric cosine function of a number.", Material.BUBBLE_CORAL_FAN);
        Input<Double> value = new Input<>("value", "Value", NumberType.INSTANCE);
        Input<String> mode = new Input<>("mode", "Mode", StringType.INSTANCE).options("cos","cosh","acos");
        Input<String> inputUnit = new Input<>("input_unit", "Input Unit", StringType.INSTANCE).options("Degrees","Radians");
        Output<Double> result = new Output<>("result", "Result", NumberType.INSTANCE);

        result.valueFrom((ctx) -> {
            double valueInput = value.getValue(ctx);
            switch (mode.getValue(ctx)) {
                case "cos" -> {
                    if (inputUnit.getValue(ctx).equals("Degrees")) {
                        return Math.cos(Math.toRadians(valueInput));
                    } else {
                        return Math.cos(valueInput);
                    }
                }
                case "acos" -> {
                    if (inputUnit.getValue(ctx).equals("Degrees")) {
                        return Math.toDegrees(Math.acos(valueInput));
                    } else {
                        return Math.acos(valueInput);
                    }
                }
                case "cosh" -> {
                    return Math.cosh(valueInput);
                }
                default -> {
                    return 0.0;
                }
            }
        });
    }

    @Override
    public Node copy() {
        return new CosineNode();
    }
}