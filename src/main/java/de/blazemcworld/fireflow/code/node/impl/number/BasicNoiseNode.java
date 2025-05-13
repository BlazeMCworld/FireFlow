package de.blazemcworld.fireflow.code.node.impl.number;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.type.VectorType;
import fastnoiselite.FastNoiseLite;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class BasicNoiseNode extends Node {
    public BasicNoiseNode() {
        super("basic_noise", "Basic Noise", "Generates noise values using various algorithms and dimensions. For 2d only the x and z coordinates are used.", Items.DEAD_BRAIN_CORAL_BLOCK);
        Input<String> noiseType = new Input<>("noise_type", "Noise Type", StringType.INSTANCE).options("Simplex", "SmoothSimplex", "Perlin", "Value", "ValueCubic");
        Input<String> dimension = new Input<>("dimension", "Dimension", StringType.INSTANCE).options("3D", "2D");
        Input<Vec3d> position = new Input<>("position", "Position", VectorType.INSTANCE);
        Input<Double> frequency = new Input<>("frequency", "Frequency", NumberType.INSTANCE);
        Input<Double> octaves = new Input<>("octaves", "Octaves", NumberType.INSTANCE);
        Input<Double> gain = new Input<>("gain", "Gain", NumberType.INSTANCE);
        Input<Double> lacunarity = new Input<>("lacunarity", "Lacunarity", NumberType.INSTANCE);
        Input<Double> seed = new Input<>("seed", "Seed", NumberType.INSTANCE);
        Output<Double> output = new Output<>("output", "Output", NumberType.INSTANCE);

        output.valueFrom((ctx) -> {
            FastNoiseLite.NoiseType noiseType1 = switch (noiseType.getValue(ctx)) {
                case "Simplex" -> FastNoiseLite.NoiseType.OpenSimplex2;
                case "SmoothSimplex" -> FastNoiseLite.NoiseType.OpenSimplex2S;
                case "Perlin" -> FastNoiseLite.NoiseType.Perlin;
                case "Value" -> FastNoiseLite.NoiseType.Value;
                case "ValueCubic" -> FastNoiseLite.NoiseType.ValueCubic;
                default -> null;
            };
            if (noiseType1 != null) {
                FastNoiseLite noise = new FastNoiseLite();
                noise.SetNoiseType(noiseType1);
                noise.SetFractalGain(gain.getValue(ctx).floatValue());
                noise.SetFractalLacunarity(lacunarity.getValue(ctx).floatValue());
                noise.SetFractalOctaves(octaves.getValue(ctx).intValue());
                noise.SetFrequency(frequency.getValue(ctx).floatValue());
                noise.SetSeed(seed.getValue(ctx).intValue());
                Vec3d loc = position.getValue(ctx);
                switch (dimension.getValue(ctx)) {
                    case "3D" -> {
                        return (double) noise.GetNoise(loc.x, loc.y, loc.z);
                    }
                    case "2D" -> {
                        return (double) noise.GetNoise(loc.x, loc.z);
                    }
                    default -> {
                        return 0.0;
                    }
                }
            }
            return 0.0;
        });
    }

    @Override
    public Node copy() {
        return new BasicNoiseNode();
    }
}