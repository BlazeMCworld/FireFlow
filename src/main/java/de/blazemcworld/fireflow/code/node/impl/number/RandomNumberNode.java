package de.blazemcworld.fireflow.code.node.impl.number;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.StringType;
import net.minecraft.item.Items;

public class RandomNumberNode extends Node {
    public RandomNumberNode() {
        super("random_number", "Random Number", "Generate a random number", Items.MULE_SPAWN_EGG);
        Input<String> mode = new Input<>("mode", "Mode", StringType.INSTANCE)
                .options("Decimal", "WholeExclusive", "WholeInclusive");
        Input<Double> min = new Input<>("min", "Min", NumberType.INSTANCE);
        Input<Double> max = new Input<>("max", "Max", NumberType.INSTANCE);
        Output<Double> output = new Output<>("output", "Output", NumberType.INSTANCE);

        output.valueFrom((ctx -> {
            double outputMin = min.getValue(ctx);
            double outputMax = max.getValue(ctx);
            return switch (mode.getValue(ctx)) {
                case "Decimal" -> (Math.random() * (outputMax - outputMin) + outputMin);
                case "WholeExclusive" -> Math.floor(Math.random() * (outputMax - outputMin) + outputMin);
                case "WholeInclusive" -> Math.floor(Math.random() * ((outputMax + 1) - outputMin) + outputMin);

                default -> 0.0;
            };
        }));
    }

    @Override
    public Node copy() {
        return new RandomNumberNode();
    }
}