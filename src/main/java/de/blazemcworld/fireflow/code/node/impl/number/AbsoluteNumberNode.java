package de.blazemcworld.fireflow.code.node.impl.number;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import net.minecraft.item.Items;

public class AbsoluteNumberNode extends Node {
    public AbsoluteNumberNode() {
        super("absolute_number", "Absolute Value", "Converts a number to its absolute value (positive equivalent)", Items.PISTON);
        Input<Double> value = new Input<>("value", "Value", NumberType.INSTANCE);
        Output<Double> result = new Output<>("result", "Result", NumberType.INSTANCE);

        result.valueFrom((ctx -> Math.abs(value.getValue(ctx))));
    }

    @Override
    public Node copy() {
        return new AbsoluteNumberNode();
    }
}