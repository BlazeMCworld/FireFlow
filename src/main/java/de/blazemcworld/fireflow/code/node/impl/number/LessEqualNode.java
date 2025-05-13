package de.blazemcworld.fireflow.code.node.impl.number;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.NumberType;
import net.minecraft.item.Items;

public class LessEqualNode extends Node {

    public LessEqualNode() {
        super("less_equal", "Less Equal", "Checks if the left number is less than or equal to the right number", Items.CARROT);

        Input<Double> left = new Input<>("left", "Left", NumberType.INSTANCE);
        Input<Double> right = new Input<>("right", "Right", NumberType.INSTANCE);
        Output<Boolean> result = new Output<>("result", "Result", ConditionType.INSTANCE);

        result.valueFrom((ctx) -> left.getValue(ctx) <= right.getValue(ctx));
    }

    @Override
    public Node copy() {
        return new LessEqualNode();
    }
}
