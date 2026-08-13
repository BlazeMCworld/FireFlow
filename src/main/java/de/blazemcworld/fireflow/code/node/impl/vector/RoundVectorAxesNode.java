package de.blazemcworld.fireflow.code.node.impl.vector;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.type.VectorType;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class RoundVectorAxesNode extends Node {

    public RoundVectorAxesNode() {
        super("round_vector_axes", "Round Vector Axes", "Rounds each axis of a vector", Items.CLAY_BALL);

        Input<Vec3> vector = new Input<>("vector", "Vector", VectorType.INSTANCE);
        Input<String> mode = new Input<>("mode", "Mode", StringType.INSTANCE).options("Round", "Floor", "Ceil");
        Output<Vec3> rounded = new Output<>("rounded", "Result", VectorType.INSTANCE);

        rounded.valueFrom(ctx -> {
            Vec3 v = vector.getValue(ctx);
            switch (mode.getValue(ctx)) {
                case "Round" -> {
                    return new Vec3(
                        Math.round(v.x),
                        Math.round(v.y),
                        Math.round(v.z)
                    );
                }
                case "Floor" -> {
                    return new Vec3(
                        Math.floor(v.x),
                        Math.floor(v.y),
                        Math.floor(v.z)
                    );
                }
                case "Ceil" -> {
                    return new Vec3(
                        Math.ceil(v.x),
                        Math.ceil(v.y),
                        Math.ceil(v.z)
                    );
                }
            }
            return v;
        });
    }

    @Override
    public Node copy() {
        return new RoundVectorAxesNode();
    }

}

