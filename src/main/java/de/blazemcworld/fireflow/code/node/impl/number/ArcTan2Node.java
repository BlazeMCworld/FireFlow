package de.blazemcworld.fireflow.code.node.impl.number;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.StringType;
import org.bukkit.Material;

public class ArcTan2Node extends Node {
    public ArcTan2Node() {
        super("arc_tan_2", "Arc Tangent 2", "Generates the arc tangent of 2 numbers.", Material.FIRE_CORAL_FAN);
        Input<Double> left = new Input<>("left", "Left", NumberType.INSTANCE);
        Input<Double> right = new Input<>("right", "Right", NumberType.INSTANCE);
        Input<String> inputUnit = new Input<>("input_unit", "Input Unit", StringType.INSTANCE).options("Degrees","Radians");
        Output<Double> result = new Output<>("result", "Result", NumberType.INSTANCE);

        result.valueFrom((ctx) -> {
            double leftInput = left.getValue(ctx);
            double rightInput = right.getValue(ctx);
            if(inputUnit.getValue(ctx).equals("Degrees")) {
                return Math.toDegrees(Math.atan2(leftInput, rightInput));
            } else {
                return Math.atan2(leftInput, rightInput);
            }
        });
    }

    @Override
    public Node copy() {
        return new ArcTan2Node();
    }
}