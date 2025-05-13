package de.blazemcworld.fireflow.code.node.impl.number;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import net.minecraft.item.Items;

public class ClampNumberNode extends Node {
    public ClampNumberNode() {
        super("clamp_number", "Clamp Number", "Clamps a number between a minimum and maximum value", Items.IRON_DOOR);
        Input<Double> number = new Input<>("input", "Number", NumberType.INSTANCE);
        Input<Double> min = new Input<>("min", "Minimum", NumberType.INSTANCE);
        Input<Double> max = new Input<>("max", "Maximum", NumberType.INSTANCE);
        Output<Double> output = new Output<>("output", "Clamped Value", NumberType.INSTANCE);

        output.valueFrom((ctx -> Math.clamp(number.getValue(ctx), min.getValue(ctx), max.getValue(ctx))));
    }

    @Override
    public Node copy() {
        return new ClampNumberNode();
    }
}