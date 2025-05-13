package de.blazemcworld.fireflow.code.node.impl.vector;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.VectorType;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class UnpackVectorNode extends Node {
    public UnpackVectorNode() {
        super("unpack_vector", "Unpack Vector", "Unpacks a vector into its components", Items.IRON_INGOT);

        Input<Vec3d> vector = new Input<>("vector", "Vector", VectorType.INSTANCE);
        Output<Double> x = new Output<>("x", "X", NumberType.INSTANCE);
        Output<Double> y = new Output<>("y", "Y", NumberType.INSTANCE);
        Output<Double> z = new Output<>("z", "Z", NumberType.INSTANCE);

        x.valueFrom(ctx -> vector.getValue(ctx).x);
        y.valueFrom(ctx -> vector.getValue(ctx).y);
        z.valueFrom(ctx -> vector.getValue(ctx).z);
    }

    @Override
    public Node copy() {
        return new UnpackVectorNode();
    }
}
