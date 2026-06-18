package de.blazemcworld.fireflow.code.node.impl.number;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.StringType;
import net.minecraft.item.Items;

import java.util.Random;

public class GaussianDistributionNode extends Node {

    public GaussianDistributionNode() {
        super("gaussian_distribution", "Gaussian Distribution", "Generate a normally distributed random number", Items.POLAR_BEAR_SPAWN_EGG);
        Input<String> mode = new Input<>("mode", "Mode", StringType.INSTANCE)
                .options("Normal", "Folded");
        Input<Double> mean = new Input<>("mean", "μ (Mean midpoint)", NumberType.INSTANCE);
        Input<Double> deviation = new Input<>("max", "σ (Standard deviation)", NumberType.INSTANCE);
        Output<Double> output = new Output<>("output", "Output", NumberType.INSTANCE);

        output.valueFrom((ctx -> {
            Random random = new Random();
            double outputMean = mean.getValue(ctx);
            double outputDeviation = deviation.getValue(ctx);

            return switch (mode.getValue(ctx)) {
                case "Normal" -> outputMean + outputDeviation * random.nextGaussian();
                case "Folded" -> outputMean + outputDeviation * Math.abs(random.nextGaussian());

                default -> 0.0;
            };
        }));
    }

    @Override
    public Node copy() {
        return new GaussianDistributionNode();
    }
}