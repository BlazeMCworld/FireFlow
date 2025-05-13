package de.blazemcworld.fireflow.code.node.impl.vector;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.StringType;
import de.blazemcworld.fireflow.code.type.VectorType;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class GetVectorComponentNode extends Node {
    public GetVectorComponentNode() {
        super("get_vector_component", "Get Vector Component", "Gets a specific axis of a vector", Items.DARK_PRISMARINE);
        Input<Vec3d> vector = new Input<>("vector", "Vector", VectorType.INSTANCE);
        Input<String> axis = new Input<>("axis", "Axis", StringType.INSTANCE).options("X", "Y", "Z");
        Output<Double> output = new Output<>("output", "Output", NumberType.INSTANCE);

        output.valueFrom((ctx -> {
            Vec3d inputVector = vector.getValue(ctx);
            return switch (axis.getValue(ctx)) {
                case "X" -> inputVector.x;
                case "Y" -> inputVector.y;
                case "Z" -> inputVector.z;
                default -> 0.0;
            };
        }));
    }

    @Override
    public Node copy() {
        return new GetVectorComponentNode();
    }
}