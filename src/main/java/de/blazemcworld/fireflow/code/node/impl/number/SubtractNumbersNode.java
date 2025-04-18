package de.blazemcworld.fireflow.code.node.impl.number;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import net.minestom.server.item.Material;

public class SubtractNumbersNode extends Node {

    public SubtractNumbersNode() {
        super("subtract_numbers", Material.SHEARS);

        Input<Double> left = new Input<>("left", NumberType.INSTANCE);
        Varargs<Double> right = new Varargs<>("right", NumberType.INSTANCE);
        Output<Double> result = new Output<>("result", NumberType.INSTANCE);

        result.valueFrom((ctx) -> {
            double out = left.getValue(ctx);
            for (double v : right.getVarargs(ctx)) out -= v;
            return out;
        });
    }

    @Override
    public Node copy() {
        return new SubtractNumbersNode();
    }
}
