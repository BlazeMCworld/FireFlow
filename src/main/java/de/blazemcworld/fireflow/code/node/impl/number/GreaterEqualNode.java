package de.blazemcworld.fireflow.code.node.impl.number;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.ConditionType;
import de.blazemcworld.fireflow.code.type.NumberType;
import net.minestom.server.item.Material;

public class GreaterEqualNode extends Node {

    public GreaterEqualNode() {
        super("greater_equal", Material.SUGAR);

        Input<Double> left = new Input<>("left", NumberType.INSTANCE);
        Input<Double> right = new Input<>("right", NumberType.INSTANCE);
        Output<Boolean> result = new Output<>("result", ConditionType.INSTANCE);

        result.valueFrom((ctx) -> left.getValue(ctx) >= right.getValue(ctx));
    }

    @Override
    public Node copy() {
        return new GreaterEqualNode();
    }
}
