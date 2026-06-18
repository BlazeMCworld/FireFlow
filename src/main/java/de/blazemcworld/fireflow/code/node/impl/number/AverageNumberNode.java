package de.blazemcworld.fireflow.code.node.impl.number;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import net.minecraft.item.Items;

public class AverageNumberNode extends Node {

    public AverageNumberNode() {
        super("average_number", "Average Number", "Returns the average number of a set.", Items.RAW_COPPER);

        Varargs<Double> numbers = new Varargs<>("numbers", "Numbers", NumberType.INSTANCE);
        Output<Double> result = new Output<>("result", "Result", NumberType.INSTANCE);

        result.valueFrom((ctx) -> {
            return numbers.getVarargs(ctx)
                    .stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
        });
    }

    @Override
    public Node copy() {
        return new AverageNumberNode();
    }

}
