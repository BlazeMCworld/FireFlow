package de.blazemcworld.fireflow.code.node.impl.vector;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.VectorType;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class ReflectVectorNode extends Node {

    public ReflectVectorNode() {
        super("reflect_vector", "Reflect Vector", "Reflects a vector, given a surface normal.", Items.GLASS_PANE);

        Input<Vec3> vector = new Input<>("vector", "Vector", VectorType.INSTANCE);
        Input<Vec3> normal = new Input<>("normal", "Normal", VectorType.INSTANCE);
        Output<Vec3> reflected = new Output<>("reflected", "Reflected", VectorType.INSTANCE);

        reflected.valueFrom(ctx -> {
            Vec3 v = vector.getValue(ctx);
            Vec3 n = normal.getValue(ctx).normalize();
            return v.subtract(n.scale(v.dot(n) * 2));
        });
    }

    @Override
    public Node copy() {
        return new ReflectVectorNode();
    }

}
