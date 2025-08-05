package de.blazemcworld.fireflow.code.node.impl.number;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import org.bukkit.Material;

public class LerpNumbersNode extends Node {

    public LerpNumbersNode() {
        super("lerp_numbers", "Lerp", "Calculates a value between two inputs by linearly interpolating based on a factor t, where t = 0 returns the start value, t = 1 returns the end value, and values in between produce a proportional blend.", Material.REPEATER);

        Input<Double> left = new Input<>("left", "Left", NumberType.INSTANCE);
        Input<Double> right = new Input<>("right", "Right", NumberType.INSTANCE);
        Input<Double> time = new Input<>("time", "Time (0-1)", NumberType.INSTANCE);
        Output<Double> result = new Output<>("result", "Result", NumberType.INSTANCE);

        result.valueFrom((ctx) -> {
            double leftValue = left.getValue(ctx);
            double rightValue = right.getValue(ctx);
            double timeValue = Math.clamp(time.getValue(ctx),0,1);
            return leftValue + (rightValue - leftValue) * timeValue;
        });
    }

    @Override
    public Node copy() {
        return new LerpNumbersNode();
    }
}
