package de.blazemcworld.fireflow.code.node.impl.vector;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.NumberType;
import de.blazemcworld.fireflow.code.type.VectorType;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class SetVectorLengthNode extends Node {
    public SetVectorLengthNode() {
        super("set_vector_length", "Set Vector Length", "Changes the length of a vector", Items.SHEARS);
        Input<Vec3> vector = new Input<>("vector", "Vector", VectorType.INSTANCE);
        Input<Double> length = new Input<>("length", "Length", NumberType.INSTANCE);
        Output<Vec3> scaled = new Output<>("scaled", "Scaled", VectorType.INSTANCE);

        scaled.valueFrom(ctx -> {
            Vec3 v = vector.getValue(ctx);
            double l = length.getValue(ctx);
            return v.normalize().scale(l);
        });
    }

    @Override
    public Node copy() {
        return new SetVectorLengthNode();
    }
}
