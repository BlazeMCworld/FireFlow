package de.blazemcworld.fireflow.code.node.impl.number;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.StringType;
import org.bukkit.Material;

public class SineNode extends Node {
    public SineNode() {
        super("sine", "Sine", "Generates the trigonometric sine function of a number.", Material.TUBE_CORAL_FAN);
        Input<Double> value = new Input<>("value", "Value", NumberType.INSTANCE);
        Input<String> mode = new Input<>("mode", "Mode", StringType.INSTANCE).options("sin","sinh","asin");
        Input<String> inputUnit = new Input<>("input_unit", "Input Unit", StringType.INSTANCE).options("Degrees","Radians");
        Output<Double> result = new Output<>("result", "Result", NumberType.INSTANCE);

        result.valueFrom((ctx) -> {
            double valueInput = value.getValue(ctx);
            switch (mode.getValue(ctx)) {
                case "sin" -> {
                    if (inputUnit.getValue(ctx).equals("Degrees")) {
                        return Math.sin(Math.toRadians(valueInput));
                    } else {
                        return Math.sin(valueInput);
                    }
                }
                case "asin" -> {
                    if (inputUnit.getValue(ctx).equals("Degrees")) {
                        return Math.toDegrees(Math.asin(valueInput));
                    } else {
                        return Math.asin(valueInput);
                    }
                }
                case "sinh" -> {
                    return Math.sinh(valueInput);
                }
                default -> {
                    return 0.0;
                }
            }
        });
    }

    @Override
    public Node copy() {
        return new SineNode();
    }
}