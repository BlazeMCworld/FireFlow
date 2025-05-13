package de.blazemcworld.fireflow.code.node.impl.vector;

import de.blazemcworld.fireflow.code.node.Node;
import de.blazemcworld.fireflow.code.type.VectorType;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class AddVectorsNode extends Node {

    public AddVectorsNode() {
        super("add_vectors", "Add Vectors", "Adds together each axis independently.", Items.ANVIL);

        Input<Vec3d> first = new Input<>("first", "First", VectorType.INSTANCE);
        Input<Vec3d> second = new Input<>("second", "Second", VectorType.INSTANCE);
        Output<Vec3d> result = new Output<>("result", "Result", VectorType.INSTANCE);

    	result.valueFrom(ctx -> first.getValue(ctx).add(second.getValue(ctx)));
    }

    @Override
    public Node copy() {
        return new AddVectorsNode();
    }
}
