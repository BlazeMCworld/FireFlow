package de.blazemcworld.fireflow.code.node.impl.number;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import net.minecraft.item.Items;

public class RemainderNode extends Node {

    public RemainderNode() {
        super("remainder_number", "Remainder", "Calculates the remainder of two numbers", Items.BONE);

        Input<Double> left = new Input<>("left", "Left", NumberType.INSTANCE);
        Input<Double> right = new Input<>("right", "Right", NumberType.INSTANCE);
        Output<Double> result = new Output<>("result", "Result", NumberType.INSTANCE);

        result.valueFrom((ctx) -> left.getValue(ctx) % right.getValue(ctx));
    }

    @Override
    public Node copy() {
        return new RemainderNode();
    }
}
