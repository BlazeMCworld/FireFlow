package de.blazemcworld.fireflow.code.node.impl.number;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.NumberType;
import net.minecraft.item.Items;

public class GreaterEqualNode extends Node {

    public GreaterEqualNode() {
        super("greater_equal", "Greater or Equal", "Checks if the left number is greater than or equal to the right number", Items.SUGAR);

        Input<Double> left = new Input<>("left", "Left", NumberType.INSTANCE);
        Input<Double> right = new Input<>("right", "Right", NumberType.INSTANCE);
        Output<Boolean> result = new Output<>("result", "Result", ConditionType.INSTANCE);

        result.valueFrom((ctx) -> left.getValue(ctx) >= right.getValue(ctx));
    }

    @Override
    public Node copy() {
        return new GreaterEqualNode();
    }
}
