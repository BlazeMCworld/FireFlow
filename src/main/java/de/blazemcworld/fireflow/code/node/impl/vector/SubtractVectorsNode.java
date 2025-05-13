package de.blazemcworld.fireflow.code.node.impl.vector;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.VectorType;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class SubtractVectorsNode extends Node {

    public SubtractVectorsNode() {
        super("subtract_vectors", "Subtract Vectors", "Subtracts the second vector from the first.", Items.ANVIL);

        Input<Vec3d> first = new Input<>("first", "First", VectorType.INSTANCE);
        Input<Vec3d> second = new Input<>("second", "Second", VectorType.INSTANCE);
        Output<Vec3d> result = new Output<>("result", "Result", VectorType.INSTANCE);

    	result.valueFrom(ctx -> first.getValue(ctx).subtract(second.getValue(ctx)));
    }

    @Override
    public Node copy() {
        return new SubtractVectorsNode();
    }
}
