package de.blazemcworld.fireflow.code.node.impl.number;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import org.bukkit.Material;

public class MinNumberNode extends Node {

    public MinNumberNode() {
        super("min_number", "Minimum Number", "Returns the lowest number in a set.", Material.DISC_FRAGMENT_5);

        Varargs<Double> numbers = new Varargs<>("numbers", "Numbers", NumberType.INSTANCE);
        Output<Double> result = new Output<>("result", "Result", NumberType.INSTANCE);

        result.valueFrom((ctx) -> {
            return numbers.getVarargs(ctx)
                    .stream()
                    .min(Double::compareTo)
                    .orElse(0.0);
        });
    }

    @Override
    public Node copy() {
        return new MinNumberNode();
    }

}
