package de.blazemcworld.fireflow.code.node.impl.vector;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.type.VectorType;
import org.bukkit.Material;
import org.bukkit.util.Vector;

public class GetVectorLengthNode extends Node {
    public GetVectorLengthNode() {
        super("get_vector_length", "Get Vector Length", "Gets the length of a vector", Material.BREEZE_ROD);
        Input<Vector> vector = new Input<>("vector", "Vector", VectorType.INSTANCE);
        Input<String> lengthType = new Input<>("length_type", "Length Type", StringType.INSTANCE).options("Normal","Squared");
        Output<Double> length = new Output<>("length", "Length", NumberType.INSTANCE);

        length.valueFrom(ctx -> {
            Vector v = vector.getValue(ctx);
            switch(lengthType.getValue(ctx)) {
                case "Normal" -> {
                    return v.length();
                }
                case "Squared" -> {
                    return v.lengthSquared();
                }
                default -> {
                    return 0.0;
                }
            }
        });
    }

    @Override
    public Node copy() {
        return new GetVectorLengthNode();
    }
}
