package de.blazemcworld.fireflow.code.node.impl.number;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import org.bukkit.Material;

public class MaxNumberNode extends Node {

    public MaxNumberNode() {
        super("max_number", "Maximum Number", "Returns the highest number in a set.", Material.NETHERITE_SCRAP);

        Varargs<Double> numbers = new Varargs<>("numbers", "Numbers", NumberType.INSTANCE);
        Output<Double> result = new Output<>("result", "Result", NumberType.INSTANCE);

        result.valueFrom((ctx) -> {
            return numbers.getVarargs(ctx)
                    .stream()
                    .max(Double::compareTo)
                    .orElse(0.0);
        });
    }

    @Override
    public Node copy() {
        return new MaxNumberNode();
    }

}
