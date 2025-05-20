package de.blazemcworld.fireflow.code.node.impl.vector;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.VectorType;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class ReflectVectorNode extends Node {

    public ReflectVectorNode() {
        super("reflect_vector", "Reflect Vector", "Reflects a vector, given a surface normal.", Items.GLASS_PANE);

        Input<Vec3d> vector = new Input<>("vector", "Vector", VectorType.INSTANCE);
        Input<Vec3d> normal = new Input<>("normal", "Normal", VectorType.INSTANCE);
        Output<Vec3d> reflected = new Output<>("reflected", "Reflected", VectorType.INSTANCE);

        reflected.valueFrom(ctx -> {
            Vec3d v = vector.getValue(ctx);
            Vec3d n = normal.getValue(ctx).normalize();
            return v.subtract(n.multiply(v.dotProduct(n) * 2));
        });
    }

    @Override
    public Node copy() {
        return new ReflectVectorNode();
    }

}
